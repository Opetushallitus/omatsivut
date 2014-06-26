package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.http.HttpClient
import fi.vm.sade.omatsivut.{AppConfig, Logging}
import org.json4s._
import org.json4s.jackson.JsonMethods._

object AuthenticationInfoService extends Logging with HttpClient {
  def getHenkiloOID(hetu : String) : Option[String] = {
    val settings = AppConfig.loadSettings.authenticationService
    implicit val formats = DefaultFormats
    val ticket = CASClient.getServiceTicket(settings)

    val (responseCode, headersMap, resultString) = httpGet(settings.url + "/" + settings.path + "/" + hetu)
      .param("ticket", ticket.getOrElse("no_ticket"))
      .responseWithHeaders

    responseCode match {
      case 404 => None
      case _ => {
        val json = parse(resultString)
        logger.info("Got user info: " + json)
        (for {
           JObject(child) <- json
           JField("oidHenkilo", JString(oid))  <- child
        } yield oid).headOption
      }
    }
  }
}