package fi.vm.sade.omatsivut

import org.scalatra._
import java.util.logging.Logger
import java.util.logging.Level
import fi.vm.sade.omatsivut.http.HttpClient

class OHPServlet extends OmatsivutStack with HttpClient {

  val settings = AppConfig.loadSettings
  val log = Logger.getLogger(getClass().getSimpleName())

  get("/applications") {
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

}
