package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.{RemoteApplicationConfig, SecuritySettings}
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.utils.cas.{CasAuthenticatingClient, CasClient, CasParams}
import fi.vm.sade.utils.slf4j.Logging
import org.http4s._
import org.http4s.client.blaze
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scalaz.concurrent.Task

trait AuthenticationInfoService {
  def getHenkiloOID(hetu: String): Option[String]
}

class StubbedAuthenticationInfoService() extends AuthenticationInfoService {
  override def getHenkiloOID(hetu: String): Option[String] = {
    TestFixture.persons.get(hetu)
  }
}

class RemoteAuthenticationInfoService(val remoteAppConfig: RemoteApplicationConfig, val securitySettings: SecuritySettings) extends AuthenticationInfoService with Logging {
  private val blazeHttpClient = blaze.defaultClient
  private val casClient = new CasClient(securitySettings.casUrl, blazeHttpClient)
  private val serviceUrl = remoteAppConfig.url + "/"
  private val casParams = CasParams(serviceUrl, securitySettings.casUsername, securitySettings.casPassword)
  private val httpClient = CasAuthenticatingClient(casClient, casParams, blazeHttpClient, Some("omatsivut.omatsivut.backend"), "JSESSIONID")
  private val callerIdHeader = Header("Caller-Id", "omatsivut.omatsivut.backend")

  private def uriFromString(url: String): Uri = {
    Uri.fromString(url).toOption.get
  }

  type Decode[ResultType] = (Int, String, Request) => ResultType

  private def runHttp[ResultType](request: Request)(decoder: (Int, String, Request) => ResultType): Task[ResultType] = {
    httpClient.fetch(request)(r => r.as[String].map(body => decoder(r.status.code, body, request)))
  }

  override def getHenkiloOID(hetu: String) : Option[String] = {
    val path = OphUrlProperties.url("oppijanumerorekisteri-service.henkiloByHetu", hetu)
    val request: Request = Request(uri = uriFromString(path), headers = Headers(callerIdHeader))

    def tryGet(retryCount: Int = 0): Option[String] =
      runHttp[Option[String]](request) {
        case (200, resultString, _) =>
          val json = parse(resultString, useBigDecimalForDouble = false)
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
