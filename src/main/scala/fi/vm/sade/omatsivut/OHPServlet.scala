package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.http.HttpClient
import org.scalatra.json._
import org.scalatra.swagger._
import org.json4s.{DefaultFormats, Formats}

class OHPServlet(implicit val swagger: Swagger) extends OmatsivutStack with HttpClient with JacksonJsonSupport with SwaggerSupport {
  protected implicit val jsonFormats: Formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all

  override def applicationName = Some("api")
  protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla voi hakea ja muokata hakemuksia ja omia tietoja"

  before() {
    contentType = formats("json")
  }
  
  val getApplicationsSwagger =  (apiOperation[List[Hakemus]]("getApplications")
      summary "Hae oppijan hakemukset"
      parameters (
        pathParam[String]("hetu").description("Käyttäjän henkilötunnus, jonka hakemukset listataan")
      )
  )

  get("/applications/:hetu", operation(getApplicationsSwagger)) {
    AuthenticationInfoService.getHenkiloOID(params("hetu")) match {
      case Some(oid) => HakemusRepository.fetchHakemukset(oid)
      case _ => response.setStatus(404)
    }
  }
}
