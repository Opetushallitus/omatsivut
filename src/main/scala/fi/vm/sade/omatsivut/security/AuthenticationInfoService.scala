package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.config.{RemoteApplicationConfig, SecuritySettings}
import fi.vm.sade.utils.http.DefaultHttpClient
import fi.vm.sade.utils.slf4j.Logging
import fi.vm.sade.security.cas.{CasClient, CasTicketRequest}
import org.json4s._
import org.json4s.jackson.JsonMethods._

class RemoteAuthenticationInfoService(val config: RemoteApplicationConfig, val securitySettings: SecuritySettings) extends Logging {
  implicit val formats = DefaultFormats

  def getHenkiloOID(hetu : String) : Option[String] = {
    val ticketRequest = CasTicketRequest(config.url + "/" + config.ticketConsumerPath, securitySettings.casUsername, securitySettings.casPassword)
    new CasClient(securitySettings.casConfig).getServiceTicket(ticketRequest).flatMap { serviceTicket =>
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
