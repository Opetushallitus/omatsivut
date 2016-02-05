package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.config.{RemoteApplicationConfig, SecuritySettings}
import fi.vm.sade.utils.cas.{CasParams, CasAuthenticatingClient, CasClient}
import org.http4s._
import org.http4s.client.blaze
import org.http4s.client.blaze.BlazeClient
import fi.vm.sade.utils.slf4j.Logging
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scalaz.concurrent.Task

class RemoteAuthenticationInfoService(val config: RemoteApplicationConfig, val securitySettings: SecuritySettings) extends Logging {
  private val blazeHttpClient: BlazeClient = blaze.defaultClient
  private val casClient = new CasClient(securitySettings.casUrl, blazeHttpClient)
  private val serviceUrl = config.url + "/"
  private val casParams = CasParams(serviceUrl, securitySettings.casUsername, securitySettings.casPassword)
  private val httpClient = new CasAuthenticatingClient(casClient, casParams, blazeHttpClient)
  private val callerIdHeader = Header("Caller-Id", "omatsivut.omatsivut.backend")

  def uriFromString(url: String): Uri = {
    Uri.fromString(url).toOption.get
  }

  type Decode[ResultType] = (Int, String, Request) => ResultType

  private def runHttp[ResultType](request: Request)(decoder: (Int, String, Request) => ResultType): Task[ResultType] = {
    for {
      response <- httpClient.apply(request)
      text <- response.as[String]
    } yield {
      decoder(response.status.code, text, request)
    }
  }

  def getHenkiloOID(hetu: String) : Option[String] = {
    val path: String = serviceUrl + config.config.getString("get_oid.path") + "/" + hetu
    val request: Request = Request(uri = uriFromString(path), headers = Headers(callerIdHeader))

    def tryGet(retryCount: Int = 0): Option[String] =
      runHttp[Option[String]](request) {
        case (200, resultString, _) =>
          val json = parse(resultString)
          val oids: List[String] = for {
            JObject(child) <- json
            JField("oidHenkilo", JString(oid)) <- child
          } yield oid
          oids.headOption
        case (401, resultString, _) if retryCount < 2 =>
          logger.warn(s"Error fetching personOid (retrying, retryCount=$retryCount). Response code=401, content=$resultString")
          tryGet(retryCount + 1)
        case (404, _, _) => None
        case (code, resultString, uri) =>
          logger.error(s"Error fetching personOid (not retrying, retryCount=$retryCount). Response code=$code, content=$resultString")
          None

      }.run
    tryGet()
  }
}
