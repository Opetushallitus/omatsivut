package fi.vm.sade.omatsivut

import java.util.logging.Logger
import java.util.logging.Level
import fi.vm.sade.omatsivut.http.HttpClient
import org.scalatra.json._
import org.scalatra.swagger._
import org.json4s.{DefaultFormats, Formats}

class OHPServlet(implicit val swagger: Swagger) extends OmatsivutStack with HttpClient with JacksonJsonSupport with SwaggerSupport {

  val settings = AppConfig.loadSettings
  val log = Logger.getLogger(getClass().getSimpleName())
  val repository = new HakemusRepository
  protected implicit val jsonFormats: Formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
 
  protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla voi hakea ja muokata hakemuksia ja omia tietoja"

  before() {
    contentType = formats("json")
  }
  
  get("/applications2") {
    try {

      val ticket = CASClient.getServiceTicket(settings.hakuAppUrl + "/" + settings.hakuAppTicketConsumer, settings.hakuAppUsername, settings.hakuAppPassword)

      val response = httpGet(settings.hakuAppUrl + "/" + settings.hakuAppHakuQuery)
        .param("ticket", ticket.getOrElse("no_ticket"))
        .response

      log.log(Level.INFO, "Got applications: " + response)
      response
    } catch {
      case t: Throwable => {
        log.log(Level.SEVERE, "Error retrieving applications", t)
        """{status: "error"}"""
      }
    }
  }

  val getApplicationsSwagger =  (apiOperation[List[Hakemus]]("getApplications")
      summary "Hae oppijan hakemukset"
      parameters (
        pathParam[String]("hetu").description("Käyttäjän henkilötunnus, jonka hakemukset listataan")
      )
  )
  get("/applications/:hetu", operation(getApplicationsSwagger)) {
    repository.fetchHakemukset(params("hetu"))
  }

}
