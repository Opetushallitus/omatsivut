package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.config.{RemoteApplicationConfig, SecuritySettings}
import fi.vm.sade.utils.cas.{CasClient, CasTicketRequest}
import fi.vm.sade.utils.http.{DefaultHttpClient, HttpRequest}
import fi.vm.sade.utils.slf4j.Logging
import org.json4s._
import org.json4s.jackson.JsonMethods._

class RemoteAuthenticationInfoService(val config: RemoteApplicationConfig, val securitySettings: SecuritySettings) extends Logging {
  implicit val formats = DefaultFormats

  private def getCookies(createNewSession: Boolean): List[String] =
    new CasClient(securitySettings.casConfig).
      getSessionCookies(CasTicketRequest(config.url, securitySettings.casUsername, securitySettings.casPassword), createNewSession)

  private def addHeaders(request: HttpRequest, createNewSession: Boolean = false): HttpRequest = {
    request
      .header("Cookie", getCookies(createNewSession).mkString("; "))
      .header("Caller-Id", "omatsivut.omatsivut.backend")
  }

  def getHenkiloOID(hetu: String) : Option[String] = {
    def tryGet(h: String, createNewSession: Boolean = false, retryCount: Int = 0): Option[String] = {
      val path: String = config.url + "/" + config.config.getString("get_oid.path") + "/" + hetu
      val request = addHeaders(DefaultHttpClient.httpGet(path), createNewSession)
      val (responseCode, headersMap, resultString) = request.responseWithHeaders()

      responseCode match {
        case 401 if retryCount < 2 =>
          tryGet(h, createNewSession = true, retryCount + 1)
        case 404 => None
        case 200 =>
          val json = parse(resultString)
          val oids: List[String] = for {
            JObject(child) <- json
            JField("oidHenkilo", JString(oid)) <- child
          } yield oid
          oids.headOption
        case code =>
          logger.error("Error fetching personOid. Response code=" + code + ", content=" + resultString)
          None
      }
    }

    tryGet(hetu)
  }
}
