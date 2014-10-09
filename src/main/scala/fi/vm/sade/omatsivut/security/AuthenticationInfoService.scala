package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.config.RemoteApplicationConfig
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.http.DefaultHttpClient
import fi.vm.sade.omatsivut.util.Logging
import org.json4s._
import org.json4s.jackson.JsonMethods._

trait AuthenticationInfoComponent {
  val authenticationInfoService: AuthenticationInfoService

  class RemoteAuthenticationInfoService(config: RemoteApplicationConfig, appConfig: AppConfig) extends AuthenticationInfoService with Logging {
    implicit val formats = DefaultFormats

    def getHenkiloOID(hetu : String) : Option[String] = {
      new CASClient(DefaultHttpClient, appConfig.settings.casTicketUrl).getServiceTicket(config) match {
        case None => None
        case Some(ticket) => getHenkiloOID(hetu, ticket)
      }
    }

    private def getHenkiloOID(hetu: String, serviceTicket: String): Option[String] = {
      val path: String = config.url + "/" + config.config.getString("get_oid.path") + "/" + hetu
      val (responseCode, headersMap, resultString) = DefaultHttpClient.httpGet(path)
           .param("ticket", serviceTicket)
           .responseWithHeaders

      responseCode match {
        case 404 => None
        case 200 => {
          val json = parse(resultString)
          val oids: List[String] = for {
            JObject(child) <- json
            JField("oidHenkilo", JString(oid)) <- child
          } yield oid
          oids.headOption
        }
        case code => {
          logger.error("Error fetching personOid. Response code=" + code + ", content=" + resultString)
          None
        }
      }
    }
  }
}

trait AuthenticationInfoService extends Logging {
  def getHenkiloOID(hetu : String) : Option[String]
}
