package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.http.HttpClient
import org.json4s.jackson.JsonMethods._
import org.json4s._

object AuthenticationInfoService extends Logging with HttpClient {
  def getHenkiloOID(hetu : String)  = {
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