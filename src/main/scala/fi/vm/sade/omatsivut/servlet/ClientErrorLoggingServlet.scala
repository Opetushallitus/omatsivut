package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori.HakemusEditoriUserContext
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.user.Oppija
import fi.vm.sade.omatsivut.security.AuthenticationRequiringServlet
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import org.scalatra.ScalatraServlet
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
      val asMap = parsedBody.extract[Map[String, String]]
      frontLogger.error("Error from frontend : user (" + user().oid + "), error breakdown: " + asMap.mkString(", "))
    } catch {
      case t: Throwable =>
        logger.error("Error when trying to log frontend error for user (" + user().oid + "). Error: " + t + " ,Request: " + request)
        response.setStatus(500)
    }
  }

}
