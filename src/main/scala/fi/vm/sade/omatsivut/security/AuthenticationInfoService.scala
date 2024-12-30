package fi.vm.sade.omatsivut.security


import cats.conversions.all.autoConvertProfunctorVariance
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.cas.{CasAuthenticatingClient, CasClient, CasParams}
import fi.vm.sade.omatsivut.config.{RemoteApplicationConfig, SecuritySettings}
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.util.Logging
import cats.effect._
import cats.effect.unsafe.IORuntime
import org.http4s._
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.typelevel.ci.CIStringSyntax

import scala.concurrent.ExecutionContext.global


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
  val runtime: IORuntime = IORuntime.global
  private def buildHttpClient: Resource[IO, Client[IO]] =
    BlazeClientBuilder[IO](global).resource

  override def getOnrHenkilo(hetu: String) : Option[OnrHenkilo] = {
    implicit val formats: Formats = DefaultFormats
    val uri = Uri.unsafeFromString(OphUrlProperties.url("oppijanumerorekisteri-service.henkiloByHetu", hetu))
    val request = Request[IO](
      method = Method.GET,
      uri = uri
    ).withHeaders(
      Header.Raw(ci"Caller-Id", AppConfig.callerId),
    )

    buildHttpClient.use { baseClient =>
      val httpClient = CasAuthenticatingClient(
        casClient = casOppijaClient,
        casParams = casParams,
        serviceClient = baseClient,
        clientCallerId = AppConfig.callerId
      )

      httpClient.run(request).use { response =>
        response.status.code match {
          case 200 =>
            response.as[String].map { responseBody =>
              val json = parse(responseBody, useBigDecimalForDouble = false)
              Some(json.extract[OnrHenkilo])
            }
          case 404 =>
            IO.pure(None)
          case statusCode =>
            response.as[String].flatMap { responseBody =>
              IO {
                logger.error(s"Error fetching person ONR info. Response code=$statusCode, content=$responseBody")
              }.as(None)
            }
        }
      }
    }.unsafeRunSync()(runtime)
  }
}
