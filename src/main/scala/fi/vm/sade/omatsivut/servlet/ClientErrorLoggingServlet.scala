package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import org.scalatra.{InternalServerError, Ok, ScalatraServlet}
import org.scalatra.json.JacksonJsonSupport
import org.slf4j.LoggerFactory

class ClientErrorLoggingServlet(val appConfig: AppConfig) extends ScalatraServlet with OmatSivutServletBase
                                                                  with JacksonJsonSupport with JsonFormats {

  private val frontLogger = LoggerFactory.getLogger("frontend")

  post("/") {
    before() {
      contentType = formats("json")
    }
    try {
      val stringToLog = parsedBody.extract[Map[String, Any]].map {case (k, v) => k + ": " + v}.mkString(" | ")
      if (stringToLog.contains("statusCode: 401")) {
        // http 401 ei ole varsinainen virhe
        frontLogger.warn("Error from frontend - " + stringToLog)
      } else {
        frontLogger.error("Error from frontend - " + stringToLog)
      }
      Ok()
    } catch {
      case t: Throwable =>
        logger.error("Error when trying to log frontend error:: " + t + ", Request: " + request)
        InternalServerError()
    }
  }
}
