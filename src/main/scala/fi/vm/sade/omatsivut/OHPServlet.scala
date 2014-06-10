package fi.vm.sade.omatsivut

import java.util.logging.Logger
import java.util.logging.Level
import fi.vm.sade.omatsivut.http.HttpClient
import org.scalatra.json._
import org.json4s.{DefaultFormats, Formats}


class OHPServlet extends OmatsivutStack with HttpClient with JacksonJsonSupport {

  val settings = AppConfig.loadSettings
  val log = Logger.getLogger(getClass().getSimpleName())
  val repository = new HakemusRepository
  protected implicit val jsonFormats: Formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all

  get("/applications2") {
    contentType = "application/json;charset=UTF-8"
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

  before() {
    contentType = formats("json")
  }

  get("/applications/:hetu") {
    repository.fetchHakemukset(params("hetu"))
  }

}
