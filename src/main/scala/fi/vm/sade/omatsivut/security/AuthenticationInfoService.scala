package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.AppConfig.{AppConfig, MockAuthentication}
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.http.HttpClient
import fi.vm.sade.omatsivut.{Logging, RemoteApplicationConfig}
import org.json4s._
import org.json4s.jackson.JsonMethods._
import fi.vm.sade.omatsivut.http.DefaultHttpClient

object AuthenticationInfoService {
  def apply(implicit appConfig: AppConfig): AuthenticationInfoService = appConfig match {
    case x: MockAuthentication => new AuthenticationInfoService {
      def getHenkiloOID(hetu: String) = hetu match {
        case TestFixture.testHetu => Some(TestFixture.personOid)
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

  implicit val formats = DefaultFormats

  def getHenkiloOID(hetu : String) : Option[String] = {
    CASClient(DefaultHttpClient).getServiceTicket(config) match {
      case None => None
      case Some(ticket) => getHenkiloOID(hetu, ticket)
    }
  }
 
  private def getHenkiloOID(hetu: String, serviceTicket: String): Option[String] = {
    val (responseCode, headersMap, resultString) = DefaultHttpClient.httpGet(config.url + "/" + config.config.getString("get_oid.path") + "/" + hetu)
         .param("ticket", serviceTicket)
         .responseWithHeaders

    responseCode match {
      case 404 => None
      case _ => {
        val json = parse(resultString)
        logger.info("Got user info: " + json)
        val oids: List[String] = for {
          JObject(child) <- json
          JField("oidHenkilo", JString(oid)) <- child
        } yield oid
        oids.headOption
      }
    }
  }
}