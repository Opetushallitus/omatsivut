package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.{AppConfig, RemoteApplicationConfig, SecuritySettings}
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.utils.cas.{CasAuthenticatingClient, CasClient, CasParams}
import fi.vm.sade.utils.slf4j.Logging
import org.http4s._
import org.http4s.client.blaze
import org.json4s._
import org.json4s.jackson.Serialization.read
import org.json4s.jackson.JsonMethods._
import scalaz.concurrent.Task

trait AuthenticationInfoService {
  def getOnrHenkilo(hetu: String): Option[OnrHenkilo]
}

case class Kieli(id: Long, kieliKoodi: String, kieliTyyppi: String)

case class OnrHenkilo(id: Long,
                      created: Long,
                      duplicate: Boolean,
                      eiSuomalaistaHetua: Boolean,
                      aidinkieli: Kieli,
                      asiointiKieli: Kieli,
                      etunimet: String,
                      kutsumanimi: String,
                      sukunimi: String,
                      hetu: String,
                      kaikkiHetut: List[String],
                      henkiloTyyppi: String,
                      oidHenkilo: String,
                      oppijanumero: String)

class StubbedAuthenticationInfoService() extends AuthenticationInfoService {
  override def getOnrHenkilo(hetu: String): Option[OnrHenkilo] = {
    TestFixture.persons.get(hetu)
  }
}

class RemoteAuthenticationInfoService(val remoteAppConfig: RemoteApplicationConfig, val casOppijaClient: CasClient, val securitySettings: SecuritySettings) extends AuthenticationInfoService with Logging {
  private val serviceUrl = remoteAppConfig.url + "/"
  private val casParams = CasParams(serviceUrl, securitySettings.casVirkailijaUsername, securitySettings.casVirkailijaPassword)
  private val httpClient = CasAuthenticatingClient(casOppijaClient, casParams, blaze.defaultClient, AppConfig.callerId, "JSESSIONID")
  private val callerIdHeader = Header("Caller-Id", AppConfig.callerId)

  private def uriFromString(url: String): Uri = {
    Uri.fromString(url).toOption.get
  }

  type Decode[ResultType] = (Int, String, Request) => ResultType

  private def runHttp[ResultType](request: Request)(decoder: (Int, String, Request) => ResultType): Task[ResultType] = {
    httpClient.fetch(request)(r => r.as[String].map(body => decoder(r.status.code, body, request)))
  }

  override def getOnrHenkilo(hetu: String) : Option[OnrHenkilo] = {
    val path = OphUrlProperties.url("oppijanumerorekisteri-service.henkiloByHetu", hetu)
    val request: Request = Request(uri = uriFromString(path), headers = Headers(callerIdHeader))

    def tryGet(retryCount: Int = 0): Option[OnrHenkilo] =
      runHttp[Option[OnrHenkilo]](request) {
        case (200, resultString, _) =>
          val json = parse(resultString, useBigDecimalForDouble = false)
          implicit val formats = DefaultFormats
          Option(json.extract[OnrHenkilo])
          case (401, resultString, _) if retryCount < 2 =>
          logger.warn(s"Error fetching person ONR info (retrying, retryCount=$retryCount). Response code=401, content=$resultString")
          tryGet(retryCount + 1)
        case (404, _, _) => None
        case (code, resultString, uri) =>
          logger.error(s"Error fetching person ONR info (not retrying, retryCount=$retryCount). Response code=$code, content=$resultString")
          None
      }.unsafePerformSync
    tryGet()
  }
}
