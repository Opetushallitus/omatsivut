package fi.vm.sade.hakemuseditori

import fi.vm.sade.hakemuseditori.auditlog.{AuditContext, AuditLogger, AuditLoggerComponent}
import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.hakemus._
import fi.vm.sade.hakemuseditori.hakemus.domain.HakemusMuutos
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.koodisto.{PostOffice, KoodistoComponent, StubbedKoodistoService}
import fi.vm.sade.hakemuseditori.koulutusinformaatio.KoulutusInformaatioComponent
import fi.vm.sade.hakemuseditori.koulutusinformaatio.domain.Opetuspiste
import fi.vm.sade.hakemuseditori.localization.{Translations, TranslationsComponent}
import fi.vm.sade.hakemuseditori.lomake.LomakeRepositoryComponent
import fi.vm.sade.hakemuseditori.ohjausparametrit.OhjausparametritComponent
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaComponent
import fi.vm.sade.hakemuseditori.valintatulokset.{NoOpValintatulosService, ValintatulosService, ValintatulosServiceComponent}
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.jackson.{Serialization, JsonMethods}
import org.springframework.context.ApplicationContext

import scala.util.{Failure, Success}

trait HakemusEditoriComponent extends ApplicationValidatorComponent with TarjontaComponent with OhjausparametritComponent
    with LomakeRepositoryComponent with HakemusRepositoryComponent with ValintatulosServiceComponent with KoulutusInformaatioComponent with Logging
    with TranslationsComponent with SpringContextComponent with AuditLoggerComponent with HakemusConverterComponent with KoodistoComponent {

  def newEditor(userContext: HakemusEditoriUserContext): HakemusEditori = {
    new HakemusEditori {
      override implicit def language = userContext.language
      override def personOid() = userContext.personOid()
    }
  }

  trait HakemusEditori {
    private val applicationValidator: ApplicationValidator = newApplicationValidator
    implicit def language: Language.Language
    def personOid(): String

    def fetchByPersonOid(personOid: String): List[HakemusInfo] = hakemusRepository.fetchHakemukset(personOid)

    def fetchByHakemusOid(hakemusOid: String): Option[HakemusInfo] = hakemusRepository.getHakemus(hakemusOid)

    def opetuspisteet(asId: String, query: String, lang: Option[String]): Option[List[Opetuspiste]] = koulutusInformaatioService.opetuspisteet(asId, query, parseLang(lang))

    def koulutukset(asId: String, opetuspisteId: String, baseEducation: Option[String], vocational: String, lang: Option[String]) = {
      koulutusInformaatioService.koulutukset(asId, opetuspisteId, baseEducation, vocational, parseLang(lang))
    }

    def postitoimipaikka(postalCode: String): Option[PostOffice] = {
      koodistoService.postOffice(postalCode, language)
    }

    private def parseLang(lang: Option[String]): Language.Value = {
      lang.flatMap(Language.parse(_)).getOrElse(Language.fi)
    }

    def validateHakemus(muutos: HakemusMuutos): Option[HakemusInfo] = {
      val lomakeOpt = lomakeRepository.lomakeByOid(muutos.hakuOid)
      val hakuOpt = tarjontaService.haku(muutos.hakuOid, language)
      (lomakeOpt, hakuOpt) match {
        case (Some(lomake), Some(haku)) => {
          Some(applicationValidator.validateAndFindQuestions(lomake, muutos, haku, personOid()))
        }
        case _ => None
      }
    }

    def updateHakemus(updated: HakemusMuutos): Option[UpdateResult] = {
      val response = for {lomake <- lomakeRepository.lomakeByOid(updated.hakuOid)
                          haku <- tarjontaService.haku(lomake.oid, language)} yield {
        val errors = applicationValidator.validate(lomake, updated, haku)
        if (errors.isEmpty) {
          hakemusUpdater.updateHakemus(lomake, haku, updated, personOid()) match {
            case Success(saved) =>
              UpdateResult(200, saved)
            case Failure(e) =>
              logger.warn("Application update rejected for application " + lomake.oid + ": " + e.getMessage)
              UpdateResult(403, "error" -> "Forbidden")
          }
        } else {
          UpdateResult(400, errors)
        }
      }
      response
    }
  }
}

trait HakemusEditoriUserContext {
  def language: Language.Language
  def personOid(): String
}

case class UpdateResult(status: Int, body: Any)

abstract class StandaloneHakemusEditoriComponent(
                                         val auditContext: AuditContext,
                                         val translations: Translations
                                         ) extends HakemusEditoriComponent {

  private lazy val runningLogger = new RunnableLogger

  val auditLogger: AuditLogger = new AuditLoggerFacade(runningLogger)
  lazy val lomakeRepository = new RemoteLomakeRepository
  override lazy val hakemusConverter: HakemusConverter = new HakemusConverter
  override val valintatulosService: ValintatulosService = new NoOpValintatulosService

  override def newApplicationValidator = new ApplicationValidator
}

class StubbedHakemusEditoriContext(auditContext: AuditContext, appContext: ApplicationContext, translations: Translations) extends StandaloneHakemusEditoriComponent(auditContext, translations) {
  override lazy val springContext = new HakemusSpringContext(appContext)
  override lazy val tarjontaService = new StubbedTarjontaService
  override lazy val koodistoService = new StubbedKoodistoService
  override lazy val ohjausparametritService = new StubbedOhjausparametritService
  override lazy val koulutusInformaatioService = new StubbedKoulutusInformaatioService
}

object Json extends JsonFormats {
  import org.json4s._
  def toJson(o: AnyRef): String = {
    Serialization.write(o)
  }

  def fromJson[A](s: String): Any = {
    Serialization.read(s)
  }
}