package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.user.Oppija
import fi.vm.sade.hakemuseditori.{HakemusEditoriUserContext, UpdateResult, HakemusEditoriComponent}
import fi.vm.sade.hakemuseditori.auditlog.{AuditLogger, AuditLoggerComponent, SaveVastaanotto}
import fi.vm.sade.hakemuseditori.hakemus.domain.{HakemusMuutos, Hakemus}
import fi.vm.sade.hakemuseditori.hakemus.{SpringContextComponent, HakemusInfo, ApplicationValidatorComponent, HakemusRepositoryComponent}
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.lomake.LomakeRepositoryComponent
import fi.vm.sade.omatsivut.hakemuspreview.HakemusPreviewGeneratorComponent
import fi.vm.sade.omatsivut.security.AuthenticationRequiringServlet
import fi.vm.sade.hakemuseditori.valintatulokset.ValintatulosServiceComponent
import fi.vm.sade.hakemuseditori.valintatulokset.domain.Vastaanotto
import org.json4s.jackson.Serialization
import org.scalatra._
import org.scalatra.json._
import org.scalatra.swagger.SwaggerSupportSyntax.OperationBuilder
import org.scalatra.swagger._
import scala.util.{Failure, Success}

trait ApplicationsServletContainer {
  this: HakemusEditoriComponent with LomakeRepositoryComponent with
    HakemusRepositoryComponent with
    ValintatulosServiceComponent with
    ApplicationValidatorComponent with
    HakemusPreviewGeneratorComponent with
    SpringContextComponent with
    AuditLoggerComponent =>

  class ApplicationsServlet(val appConfig: AppConfig)(implicit val swagger: Swagger) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats with SwaggerSupport with AuthenticationRequiringServlet with HakemusEditoriUserContext {
    override def applicationName = Some("secure/applications")
    private val applicationValidator: ApplicationValidator = newApplicationValidator
    def user = Oppija(personOid())
    private val hakemusEditori = newEditor(this)

    protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla voi hakea ja muokata hakemuksia ja omia tietoja"

    before() {
      contentType = formats("json")
    }

    val getApplicationsSwagger: OperationBuilder = (apiOperation[List[HakemusInfo]]("getApplications")
      summary "Hae kirjautuneen oppijan hakemukset"
    )
    get("/", operation(getApplicationsSwagger)) {
      hakemusEditori.fetchByPersonOid(personOid())
    }

    val putApplicationsSwagger = (apiOperation[Hakemus]("putApplication")
      summary "Tallenna muokattu hakemus"
      notes "Palauttaa tallennetun hakemuksen (tyyppiä Hakemus) ok tapauksessa ja listan virheitä (tyyppiä ValidationError) virhetapauksessa"
      parameter pathParam[String]("oid").description("Hakemuksen oid")
      parameter bodyParam[HakemusMuutos]("updated").description("Päivitetty hakemus")
    )

    put("/:oid", operation(putApplicationsSwagger)) {
      val content: String = request.body
      val updated = Serialization.read[HakemusMuutos](content)
      val response: Option[ActionResult] = hakemusEditori.updateHakemus(updated)
        .map { case UpdateResult(status, body) => ActionResult(ResponseStatus(status), body, Map.empty)}
      response.getOrElse(InternalServerError("error" -> "Internal service unavailable"))
    }


    val validateApplicationsSwagger = (apiOperation[HakemusInfo]("validateApplication")
      summary "Tarkista hakemus ja palauta virheet sekä pyydettyjen kohteiden kysymykset"
      parameter pathParam[String]("oid").description("Hakemuksen oid")
      parameter bodyParam[HakemusMuutos]("muutos").description("Päivitetty hakemus")
    )
    post("/validate/:oid", operation(validateApplicationsSwagger)) {
      val muutos = Serialization.read[HakemusMuutos](request.body)

      hakemusEditori.validateHakemus(muutos) match {
        case Some(hakemusInfo) => hakemusInfo
        case _ => InternalServerError("error" -> "Internal service unavailable")
      }
    }

    val previewApplicationSwagger: OperationBuilder = (apiOperation[String]("previewApplication")
      summary "Hakemuksen esikatselu HTML-muodossa"
      parameter pathParam[String]("oid").description("Hakemuksen oid")
    )
    get("/preview/:oid", operation(previewApplicationSwagger)) {
      newHakemusPreviewGenerator(language).generatePreview(ServerContaxtPath(request), personOid(), params("oid")) match {
        case Some(previewHtml) =>
          contentType = formats("html")
          Ok(previewHtml)
        case None =>
          NotFound("error" -> "Not found")
      }
    }

    val postVastaanotaSwagger: OperationBuilder = (apiOperation[String]("previewApplication")
      summary "Tallenna opiskelupaikan vastaanottotieto"
      parameter pathParam[String]("hakemusOid").description("Hakemuksen oid")
      parameter pathParam[String]("hakuOid").description("Haun oid")
      parameter bodyParam[ClientSideVastaanotto]("vastaanotto").description("Vastaanottotilan muutostieto")
    )
    post("/vastaanota/:hakuOid/:hakemusOid", operation(postVastaanotaSwagger)) {
      val hakemusOid = params("hakemusOid")
      val hakuOid = params("hakuOid")
      if (!hakemusRepository.exists(personOid(), hakuOid, hakemusOid)) {
        NotFound("error" -> "Not found")
      } else {
        val clientVastaanotto = Serialization.read[ClientSideVastaanotto](request.body)
        val muokkaaja: String = "henkilö:" + personOid()
        val selite = "Muokkaus Omat Sivut -palvelussa"
        val vastaanotto = Vastaanotto(clientVastaanotto.hakukohdeOid, clientVastaanotto.tila, muokkaaja, selite)
        if(valintatulosService.vastaanota(hakemusOid, hakuOid, vastaanotto)) {
          auditLogger.log(SaveVastaanotto(personOid(), hakemusOid, hakuOid, vastaanotto))
          hakemusRepository.getHakemus(personOid(), hakemusOid) match {
            case Some(hakemus) => hakemus
            case _ => NotFound("error" -> "Not found")
          }
        } else {
          InternalServerError("error" -> "Not receivable")
        }
      }
    }
  }
}

case class ClientSideVastaanotto(hakukohdeOid: String, tila: String)

