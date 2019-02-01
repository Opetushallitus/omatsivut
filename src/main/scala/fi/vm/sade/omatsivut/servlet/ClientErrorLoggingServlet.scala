package fi.vm.sade.omatsivut.servlet

import java.util

import fi.vm.sade.hakemuseditori.HakemusEditoriUserContext
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.user.Oppija
import fi.vm.sade.omatsivut.security.AuthenticationRequiringServlet
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import org.scalatra.{InternalServerError, Ok, ScalatraServlet}
import org.scalatra.json.JacksonJsonSupport
import org.slf4j.LoggerFactory

class ClientErrorLoggingServlet(val appConfig: AppConfig) extends ScalatraServlet with OmatSivutServletBase
                                                                  with JacksonJsonSupport with JsonFormats {

  private val frontLogger = LoggerFactory.getLogger("frontend")

  //override def user() = Oppija(personOid())

  post("/") {
    before() {
      contentType = formats("json")
    }
    //logger.info("A frontend error from user ({}) is being logged to oph-omatsivut-frontend.log", {user().oid})
    try {
      val stringToLog = parsedBody.extract[Map[String, Any]].map {case (k, v) => k + ": " + v}.mkString(" | ")
      //frontLogger.error("Error from frontend - user " + user().oid + ", " + stringToLog)
      frontLogger.error("Error from frontend - " + stringToLog)
      Ok(body = {"Error successfully logged to backend"})
    } catch {
      case t: Throwable =>
        //logger.error("Error when trying to log frontend error for user (" + user().oid + "). Error: " + t + " ,Request: " + request)
        logger.error("Error when trying to log frontend error:: " + t + ", Request: " + request)
        InternalServerError(body = {"Error when logging to backend"}, reason = "Parsing failed")
    }
  }
}
