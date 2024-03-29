package fi.vm.sade.hakemuseditori

import fi.vm.sade.ataru.AtaruServiceComponent
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
import fi.vm.sade.hakemuseditori.oppijanumerorekisteri.OppijanumerorekisteriComponent
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaComponent
import fi.vm.sade.hakemuseditori.tarjonta.kouta.RemoteKoutaComponent
import fi.vm.sade.hakemuseditori.tarjonta.vanha.RemoteTarjontaComponent
import fi.vm.sade.hakemuseditori.user.User
import fi.vm.sade.hakemuseditori.valintatulokset.{NoOpValintatulosService, ValintatulosService, ValintatulosServiceComponent}
import fi.vm.sade.hakemuseditori.viestintapalvelu.{AccessibleHtml, Pdf, TuloskirjeComponent, TuloskirjeKind}
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.utils.slf4j.Logging
import javax.servlet.http.HttpServletRequest
import org.json4s.jackson.Serialization
import org.springframework.context.ApplicationContext

import scala.util.{Failure, Success, Try}

sealed trait HakemusResult
case class FullSuccess(hakemukset: List[HakemusInfo]) extends HakemusResult
case class PartialSuccess(hakemukset: List[HakemusInfo], exceptions: List[Throwable]) extends HakemusResult
case class FullFailure(exceptions: List[Throwable]) extends HakemusResult

trait HakemusEditoriComponent extends ApplicationValidatorComponent
  with AtaruServiceComponent
  with OppijanumerorekisteriComponent
  with RemoteTarjontaComponent
  with RemoteKoutaComponent
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

    def fetchTuloskirje(request: HttpServletRequest, personOid: String, hakuOid: String, tuloskirjeKind: TuloskirjeKind): Option[Array[Byte]] = {
      val hakemukset = fetchByPersonOid(request, personOid, DontFetch) match {
        case FullSuccess(hs) => hs.find(_.hakemus.haku.oid == hakuOid)
        case PartialSuccess(_, ts) => throw ts.head
        case FullFailure(ts) => throw ts.head
      }
      hakemukset.flatMap(hakemus => tuloskirjeService.fetchTuloskirje(request, hakuOid, hakemus.hakemus.oid, tuloskirjeKind))
    }

    def fetchByPersonOid(request: HttpServletRequest,
                         personOid: String,
                         valintatulosFetchStrategy: ValintatulosFetchStrategy): HakemusResult = {
      logger.debug(s"Fetching hakemus by person oid $personOid")
      val ataruHakemukset = Try(ataruService.findApplications(request, personOid, valintatulosFetchStrategy, language))
      val hakuAppHakemukset = oppijanumerorekisteriService.fetchAllDuplicateOids(personOid).toList
        .map(oid => Try(hakemusRepository.fetchHakemukset(request, oid, valintatulosFetchStrategy)))
      (ataruHakemukset :: hakuAppHakemukset).foldLeft((List.empty[HakemusInfo], List.empty[Throwable])) {
        case ((hs, ts), Success(hhs)) => (hs ::: hhs, ts)
        case ((hs, ts), Failure(t)) => (hs, t :: ts)
      } match {
        case (hs, Nil) => FullSuccess(hs.sortBy(_.hakemus.received).reverse)
        case (Nil, ts) => FullFailure(ts)
        case (hs, ts) => PartialSuccess(hs.sortBy(_.hakemus.received).reverse, ts)
      }
    }

    def fetchByHakemusOid(request: HttpServletRequest,
                          personOid: String,
                          hakemusOid: String,
                          valintatulosFetchStrategy: ValintatulosFetchStrategy): Option[HakemusInfo] = {
      val optFromHakemusRepository = hakemusRepository.getHakemus(request, hakemusOid, valintatulosFetchStrategy)
      if (optFromHakemusRepository.isEmpty) {
        logger.info("fetchByHakemusOid(): Hakemus repository returned no application for given hakemusOid {}. Searching from ataru.", hakemusOid)
      }

      val matchingFromHakemusRepository = optFromHakemusRepository.filter { hakemus =>
        val personOidFromHakemus = hakemus.hakemus.personOid
        val oidsMatch = personOidFromHakemus == personOid
        if (!oidsMatch) {
          logger.warn("fetchByHakemusOid(): Hakemus repository returned an application for hakemusOid {} but its personOid {}" +
            "did not match the given personOid parameter {}. Searching from ataru.", hakemusOid, personOidFromHakemus, personOid)
        }
        oidsMatch
      }

      val result: Option[HakemusInfo] = matchingFromHakemusRepository.orElse {
        val ataruApplications = ataruService.findApplications(request, personOid, valintatulosFetchStrategy, language)
        val matchingAtaruApplication = ataruApplications.find(_.hakemus.oid == hakemusOid)
        if (ataruApplications.isEmpty) {
          logger.warn("fetchByHakemusOid(): Ataru returned no applications for given personOid {}", personOid)
        } else if (matchingAtaruApplication.isEmpty) {
          logger.warn(s"fetchByHakemusOid(): Ataru returned applications for personOid $personOid but " +
            s"their hakemusOids ${ataruApplications.map(_.hakemus.oid).mkString(",")} did not match the given hakemusOid $hakemusOid")
        }
        matchingAtaruApplication
      }
      if (result.isEmpty){
          logger.warn(s"fetchByHakemusOid(): neither hakemus repository nor ataru returned a " +
            s"matching application for personOid $personOid, hakemusOid $hakemusOid")
      }
      result
    }

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

    def validateHakemus(request: HttpServletRequest, muutos: HakemusMuutos): Option[HakemusInfo] = {
      val lomakeOpt = lomakeRepository.lomakeByOid(muutos.hakuOid)
      val hakuOpt = tarjontaService.haku(muutos.hakuOid, language)
      (lomakeOpt, hakuOpt) match {
        case (Some(lomake), Some(haku)) => {
          Some(applicationValidator.validateAndFindQuestions(request, lomake, muutos, haku, user()))
        }
        case _ => None
      }
    }

    def updateHakemus(request: HttpServletRequest, updated: HakemusMuutos): Try[Hakemus] = {
      (for {lomake <- lomakeRepository.lomakeByOid(updated.hakuOid)
            haku <- tarjontaService.haku(lomake.oid, language)} yield {
        val errors = applicationValidator.validate(lomake, updated, haku)
        if (errors.isEmpty) {
          hakemusUpdater.updateHakemus(request, lomake, haku, updated, user()) match {
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
                                         val translations: Translations
                                         ) extends HakemusEditoriComponent {
  lazy val lomakeRepository = new RemoteLomakeRepository
  override lazy val hakemusConverter: HakemusConverter = new HakemusConverter
  override val valintatulosService: ValintatulosService = new NoOpValintatulosService

  override def newApplicationValidator = new ApplicationValidator
}

class StubbedHakemusEditoriContext(appContext: ApplicationContext,
                                   translations: Translations,
                                   config: AppConfig)
  extends StandaloneHakemusEditoriComponent(translations) {
  override lazy val springContext = new HakemusSpringContext(appContext)
  override lazy val ataruService = new StubbedAtaruService
  override lazy val oppijanumerorekisteriService = new StubbedOppijanumerorekisteriService
  override lazy val tarjontaService = new StubbedTarjontaService(config)
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
