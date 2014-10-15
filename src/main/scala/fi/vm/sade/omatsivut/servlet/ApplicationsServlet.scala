package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.omatsivut.auditlog.{AuditLoggerComponent, AuditLogger}
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.config.{OmatSivutSpringContext, SpringContextComponent}
import fi.vm.sade.omatsivut.hakemus._
import fi.vm.sade.omatsivut.hakemus.domain.{HakemusMuutos, ValidationError, _}
import fi.vm.sade.omatsivut.haku.domain.QuestionNode
import fi.vm.sade.omatsivut.haku.{HakuRepository, HakuRepositoryComponent}
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.security.Authentication
import fi.vm.sade.omatsivut.tarjonta.Hakuaika
import fi.vm.sade.omatsivut.util.Timer._
import fi.vm.sade.omatsivut.valintatulokset.{Vastaanotto, ValintatulosService, ValintatulosServiceComponent}
import org.json4s.jackson.Serialization
import org.scalatra.json._
import org.scalatra.swagger.SwaggerSupportSyntax.OperationBuilder
import org.scalatra.swagger._
import org.scalatra.{BadRequest, Forbidden, NotFound, Ok}
import org.scalatra.ActionResult
import org.scalatra.ActionResult
import fi.vm.sade.omatsivut.auditlog.AuditEvent
import fi.vm.sade.omatsivut.auditlog.SaveVastaanotto

trait ApplicationsServletContainer {
  this: HakuRepositoryComponent with
    HakemusRepositoryComponent with
    ValintatulosServiceComponent with
    ApplicationValidatorComponent with
    HakemusPreviewGeneratorComponent with
    SpringContextComponent with
    AuditLoggerComponent =>

  val hakuRepository: HakuRepository
  val hakemusRepository: HakemusRepository
  val springContext: OmatSivutSpringContext
  val valintatulosService: ValintatulosService

  class ApplicationsServlet(val appConfig: AppConfig)(implicit val swagger: Swagger) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats with SwaggerSupport with Authentication {
    override def applicationName = Some("api")
    private val applicationSystemService = springContext.applicationSystemService
    private val applicationValidator: ApplicationValidator = newApplicationValidator
    override val authAuditLogger: AuditLogger = auditLogger

    protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla voi hakea ja muokata hakemuksia ja omia tietoja"

    before() {
      contentType = formats("json")
    }

    val getApplicationsSwagger: OperationBuilder = (apiOperation[List[Hakemus]]("getApplications")
      summary "Hae kirjautuneen oppijan hakemukset"
    )
    get("/applications", operation(getApplicationsSwagger)) {
      hakemusRepository.fetchHakemukset(personOid())
    }

    val putApplicationsSwagger = (apiOperation[Hakemus]("putApplication")
      summary "Tallenna muokattu hakemus"
      notes "Palauttaa tallennetun hakemuksen (tyyppiä Hakemus) ok tapauksessa ja listan virheitä (tyyppiä ValidationError) virhetapauksessa"
      parameter pathParam[String]("oid").description("Hakemuksen oid")
      parameter bodyParam[HakemusMuutos]("updated").description("Päivitetty hakemus")
    )
    put("/applications/:oid", operation(putApplicationsSwagger)) {
      val content: String = request.body
      val updated = Serialization.read[HakemusMuutos](content)
      val applicationSystem = applicationSystemService.getApplicationSystem(updated.hakuOid)
      val haku = timed(1000, "Tarjonta fetch Application"){
        tarjontaService.haku(applicationSystem.getId, language)
      }
      val errors = applicationValidator.validate(applicationSystem)(updated)
      if(errors.isEmpty) {
        hakemusRepository.updateHakemus(applicationSystem, haku.get)(updated, personOid()) match {
          case Some(saved) => Ok(saved)
          case None => Forbidden()
        }
      } else {
        BadRequest(errors)
      }
    }

    val validateApplicationsSwagger = (apiOperation[ValidationResult]("validateApplication")
      summary "Tarkista hakemus ja palauta virheet sekä pyydettyjen kohteiden kysymykset"
      parameter queryParam[String]("questionsOf").description("Hakukohteiden oidit joiden kysymykset halultaan. Pilkulla eroteltuna")
      parameter pathParam[String]("oid").description("Hakemuksen oid")
      parameter bodyParam[HakemusMuutos]("muutos").description("Päivitetty hakemus")
    )
    post("/applications/validate/:oid", operation(validateApplicationsSwagger)) {
      val muutos = Serialization.read[HakemusMuutos](request.body)
      val applicationSystem = applicationSystemService.getApplicationSystem(muutos.hakuOid)
      val questionsOf: List[String] = paramOption("questionsOf").getOrElse("").split(',').toList
      val (errors: List[ValidationError], questions: List[QuestionNode], updatedApplication: Application) = applicationValidator.validateAndFindQuestions(applicationSystem)(muutos, questionsOf, personOid())
      ValidationResult(errors, questions, hakuRepository.getApplicationPeriods(applicationSystem.getId))
    }


    val previewApplicationSwagger: OperationBuilder = (apiOperation[String]("previewApplication")
      summary "Hakemuksen esikatselu HTML-muodossa"
      parameter pathParam[String]("oid").description("Hakemuksen oid")
    )
    get("/applications/preview/:oid", operation(previewApplicationSwagger)) {
      newHakemusPreviewGenerator(language).generatePreview(ServerContaxtPath(request), personOid(), params("oid")) match {
        case Some(previewHtml) =>
          contentType = formats("html")
          Ok(previewHtml)
        case None =>
          NotFound()
      }
    }

    val postVastaanotaSwagger: OperationBuilder = (apiOperation[String]("previewApplication")
      summary "Tallenna opiskelupaikan vastaanottotieto"
      parameter pathParam[String]("hakemusOid").description("Hakemuksen oid")
      parameter pathParam[String]("hakuOid").description("Haun oid")
      parameter bodyParam[ClientSideVastaanotto]("vastaanotto").description("Vastaanottotilan muutostieto")
    )
    post("/applications/vastaanota/:hakuOid/:hakemusOid", operation(postVastaanotaSwagger)) {
      val hakemusOid = params("hakemusOid")
      val hakuOid = params("hakuOid")
      if (!hakemusRepository.exists(personOid(), hakuOid, hakemusOid)) {
        response.setStatus(404)
        "Not found"
      } else {
        val clientVastaanotto = Serialization.read[ClientSideVastaanotto](request.body)
        val muokkaaja: String = "henkilö:" + personOid()
        val selite = "Muokkaus Omat Sivut -palvelussa"
        val vastaanotto = Vastaanotto(clientVastaanotto.hakukohdeOid, clientVastaanotto.tila, muokkaaja, selite)
        if(valintatulosService.vastaanota(hakemusOid, hakuOid, vastaanotto)) {
          auditLogger.log(SaveVastaanotto(personOid(), hakemusOid, vastaanotto))
        } else {
          response.setStatus(500)
        }
        hakemusRepository.getHakemus(personOid(), hakemusOid)
      }
    }
  }
}

case class ClientSideVastaanotto(hakukohdeOid: String, tila: String)

case class ValidationResult(errors: List[ValidationError], questions: List[QuestionNode], applicationPeriods: List[Hakuaika])

