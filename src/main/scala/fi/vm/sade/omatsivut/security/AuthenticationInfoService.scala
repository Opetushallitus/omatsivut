package fi.vm.sade.omatsivut.security

import fi.vm.sade.javautils.nio.cas.CasClient
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.{RemoteApplicationConfig, SecuritySettings}
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.util.Logging
import org.asynchttpclient.RequestBuilder
import org.json4s._

import scala.concurrent.ExecutionContext.Implicits.global
import org.json4s.jackson.JsonMethods._

import java.util.concurrent.TimeUnit
import scala.compat.java8.FutureConverters.toScala
import scala.concurrent.Await
import scala.concurrent.duration.Duration

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

  override def getOnrHenkilo(hetu: String) : Option[OnrHenkilo] = {
    val req = new RequestBuilder()
      .setMethod("GET")
      .setUrl(OphUrlProperties.url("oppijanumerorekisteri-service.henkiloByHetu", hetu))
      .build()
    val result = toScala(casOppijaClient.execute(req)).map {
      case r if r.getStatusCode() == 200  =>
        val json = parse(r.getResponseBody(), useBigDecimalForDouble = false)
        implicit val formats = DefaultFormats
        val henkilo = json.extract[OnrHenkilo]
        Option(henkilo)
      case r if r.getStatusCode() == 404  =>
        None
      case r =>
        logger.error(s"Error fetching person ONR info. Response code=${r.getStatusCode()}, content=${r.getResponseBody()}")
        None
    }
    Await.result(result, Duration(1, TimeUnit.MINUTES))
  }
}
