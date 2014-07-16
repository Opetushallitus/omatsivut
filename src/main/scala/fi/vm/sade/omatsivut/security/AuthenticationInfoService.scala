package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.AppConfig.{MockAuthentication, StubbedExternalDeps, AppConfig}
import fi.vm.sade.omatsivut.http.HttpClient
import fi.vm.sade.omatsivut.{RemoteApplicationConfig, AppConfig, Logging}
import org.json4s._
import org.json4s.jackson.JsonMethods._

object AuthenticationInfoService {
  def apply(implicit appConfig: AppConfig): AuthenticationInfoService = appConfig match {
    case x: MockAuthentication => new AuthenticationInfoService {
      def getHenkiloOID(hetu: String) = hetu match {
        case "010101-123N" => Some("1.2.246.562.24.14229104472")
        case _ => None
      }
    }
    case _ => new RemoteAuthenticationInfoService(appConfig.settings.authenticationServiceConfig)(appConfig)
  }
}

trait AuthenticationInfoService extends Logging {
  def getHenkiloOID(hetu : String) : Option[String]
}

class RemoteAuthenticationInfoService(config: RemoteApplicationConfig)(implicit val appConfig: AppConfig) extends AuthenticationInfoService with Logging {
  def getHenkiloOID(hetu : String) : Option[String] = {
    implicit val formats = DefaultFormats
    val ticket = CASClient().getServiceTicket(config)

    val (responseCode, headersMap, resultString) = HttpClient.httpGet(config.url + "/" + config.path + "/" + hetu)
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