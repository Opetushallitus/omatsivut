package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.http.HttpClient
import org.scalatra.json._
import org.scalatra.swagger._
import org.json4s.jackson.Serialization

class OHPServlet(implicit val swagger: Swagger) extends OmatsivutStack with HttpClient with JacksonJsonSupport with OHPJsonFormats with SwaggerSupport with Authentication {
  override def applicationName = Some("api")

  protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla voi hakea ja muokata hakemuksia ja omia tietoja"

  before() {
    contentType = formats("json")
  }

  val getApplicationsSwagger = (apiOperation[List[Hakemus]]("getApplications")
    summary "Hae oppijan hakemukset"
    parameters pathParam[String]("hetu").description("Käyttäjän henkilötunnus, jonka hakemukset listataan")
    )

  val putApplicationsSwagger = (apiOperation[Unit]("putApplication")
    summary "Tallenna hakemus"
    )

  get("/applications", operation(getApplicationsSwagger)) {
    HakemusRepository.fetchHakemukset(oid())
  }

  put("/applications/:oid", operation(putApplicationsSwagger)) {
    val updated = Serialization.read[Hakemus](request.body)
    HakemusRepository.updateHakemus(updated)
  }
}
