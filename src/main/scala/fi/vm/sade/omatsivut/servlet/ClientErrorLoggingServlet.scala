package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import org.codehaus.jackson.map.ObjectMapper
import org.scalatra.ScalatraServlet
import org.scalatra.json.JacksonJsonSupport

class ClientErrorLoggingServlet(val appConfig: AppConfig) extends ScalatraServlet with OmatSivutServletBase with JacksonJsonSupport with JsonFormats {

  //"/omatsivut/secure/errorlogtobackend"

  //TODO nämä viestit omalle logilleen, nyt menevät samaan logivirtaan kuin muutkin
  post("/*") {
    //TODO lisää autentikaatio, ehkä myös sieltä tunnistetietoja virhelogille?
    before() {
      contentType = formats("json")
    }
    try {
      val stacktrace = parsedBody.extract
      //TODO muotoile virhe kivasti jos tarpeen
      logger.warn("Error from frontend: {}", stacktrace)
    } catch {
      case t: Throwable =>
        logger.error("Fronttivirheen parsimisessa tapahtui virhe: {}. Request: {}, body: {}", t, request, request.body)
    }
  }
}
