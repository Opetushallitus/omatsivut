package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.domain.{Hakemus, QuestionNode, ValidationError}
import fi.vm.sade.omatsivut.hakemus.{ApplicationValidator, HakemusRepository}
import fi.vm.sade.omatsivut.json.{JsonConverter, JsonFormats}
import fi.vm.sade.omatsivut.security.Authentication
import org.json4s.jackson.{JsonMethods, Serialization}
import org.scalatra.json._
import org.scalatra.swagger.SwaggerSupportSyntax.OperationBuilder
import org.scalatra.swagger._
import org.scalatra.{BadRequest, Ok}

class ApplicationsServlet(implicit val swagger: Swagger, val appConfig: AppConfig) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats with SwaggerSupport with Authentication {
  override def applicationName = Some("api")

  protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla voi hakea ja muokata hakemuksia ja omia tietoja"

  val getApplicationsSwagger: OperationBuilder = (apiOperation[List[Hakemus]]("getApplications")
    summary "Hae oppijan hakemukset"
    parameters pathParam[String]("hetu").description("Käyttäjän henkilötunnus, jonka hakemukset listataan")
    )

  val putApplicationsSwagger = (apiOperation[Unit]("putApplication")
    summary "Tallenna hakemus"
    )

  val validateApplicationsSwagger = (apiOperation[Unit]("validateApplication")
    summary "Tarkista hakemus ja palauta virheet sekä kysymykset joihin ei ole vastattu"
    )

  before() {
    contentType = formats("json")
  }

  get("/applications", operation(getApplicationsSwagger)) {
    HakemusRepository().fetchHakemukset(oid())
  }

  put("/applications/:oid", operation(putApplicationsSwagger)) {
    val json = JsonMethods.parse(request.body)
    val updated = json.extract[Hakemus].copy(answers = JsonConverter.stringyfiedAnswers(json))
    val (errors, _) = ApplicationValidator().validate(updated)
    if(errors.isEmpty) {
      val saved = HakemusRepository().updateHakemus(updated)
      Ok(saved)
    } else {
      BadRequest(errors)
    }
  }

  post("/applications/validate/:oid", operation(validateApplicationsSwagger)) {
    val validate = Serialization.read[Hakemus](request.body)
    val (errors, questions) = ApplicationValidator().validate(validate)
    ValidationResult(errors, questions)
  }

  case class ValidationResult(errors: List[ValidationError], questions: List[QuestionNode])
}
