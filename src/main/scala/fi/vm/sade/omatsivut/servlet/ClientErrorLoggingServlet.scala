package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import org.codehaus.jackson.map.ObjectMapper
import org.scalatra.ScalatraServlet
import org.scalatra.json.JacksonJsonSupport
import org.slf4j.Logger

class ClientErrorLoggingServlet(val appConfig: AppConfig) extends ScalatraServlet with OmatSivutServletBase with JacksonJsonSupport with JsonFormats {

  //"/omatsivut/secure/errorlogtobackend"

  //TODO nämä viestit omalle logilleen, nyt menevät samaan logivirtaan kuin muutkin
  post("/") {
    //logger.info("V2 Saatiin virhe! data: {}", request.body)
    before() {
      contentType = formats("json")
    }
    //TODO lisää autentikaatio, ehkä myös sieltä tunnistetietoja virhelogille?
    //val errorData = request.body
    //logger.info("--- parsedbody: {}", parsedBody)
    try {
      //val e = parsedBody.extract[FrontendError]
      val asMap = parsedBody.extract[Map[String, String]]
      //logger.info("parsed FrontendError: {}", e)
      logger.info("asMap: {}", asMap)
      //val stacktrace = parsedBody.extract
      val errorUrl = asMap.getOrElse("errorUrl", "unknown url")
      val errorMessage = asMap.getOrElse("errorMessage", "no message")
      val stackTrace = asMap.getOrElse("stackTrace", "no stacktrace")
      val cause = asMap.getOrElse("cause", "unknown cause")
      val browser = asMap.getOrElse("browser", "unknown browser")
      val browserVersion = asMap.getOrElse("browserVersion", "unknown browser version")
      logger.error("Error from frontend: errorUrl: {}, errorMessage: {}, stackTrace: {}, cause: {}, browser: {}, browser version: {} ",
        errorUrl, errorMessage, stackTrace, cause, browser, browserVersion)
    } catch {
      case t: Throwable =>
        logger.error("Fronttivirheen logituksessa tapahtui virhe: {}. Request: {}, body: {}", t, request, request.body)
    }
  }

}
