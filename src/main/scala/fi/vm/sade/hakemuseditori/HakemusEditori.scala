package fi.vm.sade.hakemuseditori

import fi.vm.sade.ataru.AtaruServiceComponent
import fi.vm.sade.hakemuseditori.auditlog.{AuditContext, AuditLogger, AuditLoggerComponent}
import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.hakemus._
import fi.vm.sade.hakemuseditori.hakemus.domain.{Hakemus, HakemusMuutos, ValidationError}
import fi.vm.sade.hakemuseditori.hakumaksu.{HakumaksuComponent, StubbedHakumaksuServiceWrapper}
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.koodisto.{KoodistoComponent, PostOffice, StubbedKoodistoService}
import fi.vm.sade.hakemuseditori.koulutusinformaatio.domain.Opetuspiste
import fi.vm.sade.hakemuseditori.koulutusinformaatio.KoulutusInformaatioComponent
import fi.vm.sade.hakemuseditori.localization.{Translations, TranslationsComponent}
import fi.vm.sade.hakemuseditori.lomake.LomakeRepositoryComponent
import fi.vm.sade.hakemuseditori.ohjausparametrit.OhjausparametritComponent
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaComponent
import fi.vm.sade.hakemuseditori.user.User
import fi.vm.sade.hakemuseditori.valintatulokset.{NoOpValintatulosService, ValintatulosService, ValintatulosServiceComponent}
import fi.vm.sade.hakemuseditori.viestintapalvelu.TuloskirjeComponent
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.jackson.Serialization
import org.springframework.context.ApplicationContext

import scala.util.{Failure, Success, Try}

sealed trait HakemusResult
case class FullSuccess(hakemukset: List[HakemusInfo]) extends HakemusResult
case class PartialSuccess(hakemukset: List[HakemusInfo], exceptions: List[Throwable]) extends HakemusResult
case class FullFailure(exceptions: List[Throwable]) extends HakemusResult

trait HakemusEditoriComponent extends ApplicationValidatorComponent
  with AtaruServiceComponent
  with TarjontaComponent
  with OhjausparametritComponent
  with LomakeRepositoryComponent
  with HakemusRepositoryComponent
  with ValintatulosServiceComponent
  with KoulutusInformaatioComponent
  with Logging
  with TuloskirjeComponent
  with TranslationsComponent
  with SpringContextComponent
  with AuditLoggerComponent
  with HakemusConverterComponent
  with KoodistoComponent
  with HakumaksuComponent
  with SendMailComponent {

  def newEditor(userContext: HakemusEditoriUserContext): HakemusEditori = {
    new HakemusEditori {
      override implicit def language = userContext.language
      override def user() = userContext.user()
    }
  }

  trait HakemusEditori {
    private val applicationValidator: ApplicationValidator = newApplicationValidator
    implicit def language: Language.Language
    def user(): User

    def fetchTuloskirje(personOid: String, hakuOid: String): Option[Array[Byte]] = {
      val hakemukset = fetchByPersonOid(personOid, DontFetch) match {
        case FullSuccess(hs) => hs.find(_.hakemus.haku.oid == hakuOid)
        case PartialSuccess(_, ts) => throw ts.head
        case FullFailure(ts) => throw ts.head
      }
      hakemukset.flatMap(hakemus => tuloskirjeService.fetchTuloskirje(hakuOid, hakemus.hakemus.oid, personOid))
    }

    def fetchByPersonOid(personOid: String,
                         valintatulosFetchStrategy: ValintatulosFetchStrategy): HakemusResult = {
      (
        Try(ataruService.findApplications(personOid, valintatulosFetchStrategy)),
        Try(hakemusRepository.fetchHakemukset(personOid, valintatulosFetchStrategy))
      ) match {
        case (Success(ahs), Success(hhs)) => FullSuccess((ahs ::: hhs).sortBy[Option[Long]](_.hakemus.received).reverse)
        case (Failure(at), Failure(ht)) => FullFailure(List(at, ht))
        case (ahs, hhs) => PartialSuccess(
          (ahs.getOrElse(List.empty) ::: hhs.getOrElse(List.empty)).sortBy[Option[Long]](_.hakemus.received).reverse,
          ahs.failed.toOption.toList ::: hhs.failed.toOption.toList
        )
      }
    }

    def fetchByHakemusOid(personOid: String,
                          hakemusOid: String,
                          valintatulosFetchStrategy: ValintatulosFetchStrategy): Option[HakemusInfo] =
      hakemusRepository.getHakemus(hakemusOid, valintatulosFetchStrategy)
        .orElse(ataruService.findApplications(personOid, valintatulosFetchStrategy).find(_.hakemus.oid == hakemusOid))

    def opetuspisteet(asId: String, query: String, lang: Option[String]): Option[List[Opetuspiste]] = koulutusInformaatioService.opetuspisteet(asId, query, parseLang(lang))

    def koulutukset(asId: String, opetuspisteId: String, baseEducation: Option[String], vocational: String, lang: Option[String]) = {
      koulutusInformaatioService.koulutukset(asId, opetuspisteId, baseEducation, vocational, parseLang(lang))
    }

    def postitoimipaikka(postalCode: String): Option[PostOffice] = {
      koodistoService.postOffice(postalCode, language)
    }

    private def parseLang(lang: Option[String]): Language.Value = {
      lang.flatMap(Language.parse).getOrElse(Language.fi)
    }

    def validateHakemus(muutos: HakemusMuutos): Option[HakemusInfo] = {
      val lomakeOpt = lomakeRepository.lomakeByOid(muutos.hakuOid)
      val hakuOpt = tarjontaService.haku(muutos.hakuOid, language)
      (lomakeOpt, hakuOpt) match {
        case (Some(lomake), Some(haku)) => {
          Some(applicationValidator.validateAndFindQuestions(lomake, muutos, haku, user()))
        }
        case _ => None
      }
    }

    def updateHakemus(updated: HakemusMuutos): Try[Hakemus] = {
      (for {lomake <- lomakeRepository.lomakeByOid(updated.hakuOid)
            haku <- tarjontaService.haku(lomake.oid, language)} yield {
        val errors = applicationValidator.validate(lomake, updated, haku)
        if (errors.isEmpty) {
          hakemusUpdater.updateHakemus(lomake, haku, updated, user()) match {
            case Success(saved) => Success(saved)
            case Failure(e) =>
              logger.warn("Application update rejected for application " + lomake.oid, e)
              Failure(new ForbiddenException())
          }
        } else {
          Failure(new ValidationException(errors))
        }
      }).getOrElse(Failure(new RuntimeException("Internal service unavailable")))
    }
  }
}

trait HakemusEditoriUserContext {
  def language: Language.Language
  def user(): User
}

class ForbiddenException() extends RuntimeException("Forbidden")
class ValidationException(errors: List[ValidationError]) extends RuntimeException("Validation failed") {
  val validationErrors = errors
}

abstract class StandaloneHakemusEditoriComponent(
                                         val auditContext: AuditContext,
                                         val translations: Translations
                                         ) extends HakemusEditoriComponent {

  val auditLogger: AuditLogger = new AuditLoggerFacade()
  lazy val lomakeRepository = new RemoteLomakeRepository
  override lazy val hakemusConverter: HakemusConverter = new HakemusConverter
  override val valintatulosService: ValintatulosService = new NoOpValintatulosService

  override def newApplicationValidator = new ApplicationValidator
}

class StubbedHakemusEditoriContext(auditContext: AuditContext, appContext: ApplicationContext, translations: Translations) extends StandaloneHakemusEditoriComponent(auditContext, translations) {
  override lazy val springContext = new HakemusSpringContext(appContext)
  override lazy val ataruService = new StubbedAtaruService
  override lazy val tarjontaService = new StubbedTarjontaService
  override lazy val tuloskirjeService = new StubbedTuloskirjeService
  override lazy val koodistoService = new StubbedKoodistoService
  override lazy val ohjausparametritService = new StubbedOhjausparametritService
  override lazy val koulutusInformaatioService = new StubbedKoulutusInformaatioService
  override lazy val hakumaksuService = new StubbedHakumaksuServiceWrapper
  override lazy val sendMailService = new StubbedSendMailServiceWrapper
}

case class HakemusEditoriRemoteUrls(
                                     tarjontaUrl: String,
                                     koodistoUrl: String,
                                     ohjausparametritUrl: String,
                                     koulutusinformaatioAoUrl: String,
                                     koulutusinformaationBIUrl: String,
                                     koulutusinformaatioLopUrl: String
                                     )

object Json extends JsonFormats {
  def toJson(o: AnyRef): String = {
    Serialization.write(o)
  }

  def fromJson[A](s: String): A = {
    Serialization.read(s)
  }

  def fromJson[A](s: String, klass: Class[A]): A = {
    val manifest = Manifest.classType(klass)
    Serialization.read(s)(jsonFormats, manifest)
  }
}
