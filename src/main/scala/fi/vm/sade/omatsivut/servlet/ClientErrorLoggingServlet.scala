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

class ClientErrorLoggingServlet(val appConfig: AppConfig) extends ScalatraServlet with OmatSivutServletBase with AuthenticationRequiringServlet
                                                                  with JacksonJsonSupport with JsonFormats with HakemusEditoriUserContext {

  private val frontLogger = LoggerFactory.getLogger("frontend")

  override def user() = Oppija(personOid())

  post("/") {
    before() {
      contentType = formats("json")
    }
    try {
      val asMap = parsedBody.extract[Map[String, Any]]
      frontLogger.error("Error from frontend : user (" + user().oid + "), " + asMap.mkString(", "))
      Ok("Error successfully logged to backend")
    } catch {
      case t: Throwable =>
        logger.error("Error when trying to log frontend error for user (" + user().oid + "). Error: " + t + " ,Request: " + request)
        InternalServerError("Error when logging to backend")
    }
  }
}
