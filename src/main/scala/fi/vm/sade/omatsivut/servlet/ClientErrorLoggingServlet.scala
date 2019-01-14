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

  //override lazy val logger: Logger = LoggerFactory.getLogger("frontend")
  private val frontLogger = LoggerFactory.getLogger("frontend")

  override def user() = Oppija(personOid())

  //TODO nämä viestit omalle logilleen, nyt menevät samaan logivirtaan kuin muutkin
  post("/") {
    before() {
      contentType = formats("json")
    }
    //TODO lisää autentikaatio, ehkä myös sieltä tunnistetietoja virhelogille?
    try {
      val asMap = parsedBody.extract[Map[String, String]]
      val stringToLog: String = asMap.mkString(", ")
      logger.info("logging frontend error, user oid: {}", user.oid)
      logger.error ("Error from frontend, mainfeed: " + stringToLog)
      frontLogger.error("Error from frontend, separate feed: " + stringToLog)
    } catch {
      case t: Throwable =>
        logger.error("Fronttivirheen logituksessa tapahtui virhe: {}. Request: {}, body: {}", t, request, request.body)
    }
  }

}
