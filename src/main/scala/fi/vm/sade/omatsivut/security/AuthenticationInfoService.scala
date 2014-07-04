package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.http.HttpClient
import fi.vm.sade.omatsivut.{RemoteApplicationConfig, AppConfig, Logging}
import org.json4s._
import org.json4s.jackson.JsonMethods._

trait AuthenticationInfoService extends Logging with HttpClient {
  def getHenkiloOID(hetu : String) : Option[String]
}

class RemoteAuthenticationInfoService(config: RemoteApplicationConfig) extends AuthenticationInfoService with Logging with HttpClient {
  def getHenkiloOID(hetu : String) : Option[String] = {
    implicit val formats = DefaultFormats
    val ticket = CASClient.getServiceTicket(config)

    val (responseCode, headersMap, resultString) = httpGet(config.url + "/" + config.path + "/" + hetu)
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