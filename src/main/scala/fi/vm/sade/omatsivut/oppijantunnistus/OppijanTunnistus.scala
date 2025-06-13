package fi.vm.sade.omatsivut.oppijantunnistus

import java.util.concurrent.TimeUnit
import fi.vm.sade.javautils.nio.cas.{CasClient, CasClientBuilder, CasConfig}
import fi.vm.sade.omatsivut.NonSensitiveHakemusInfo.Oid
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.util.SharedAsyncHttpClient
import org.asynchttpclient.RequestBuilder
import org.json4s._
import org.json4s.jackson.JsonMethods

import scala.compat.java8.FutureConverters.toScala
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

trait OppijanTunnistusComponent {
  val oppijanTunnistusService: OppijanTunnistusService
}

trait OppijanTunnistusService {
  def validateToken(token: String): Try[OppijantunnistusMetadata]
}

case class OppijanTunnistusVerification(exists: Boolean, valid: Boolean, metadata: Option[OppijantunnistusMetadata])

case class OppijantunnistusMetadata(hakemusOid: Oid, personOid: Option[Oid], hakuOid: Option[Oid])

class InvalidTokenException(msg: String) extends RuntimeException(msg)

class ExpiredTokenException(msg: String) extends RuntimeException(msg)

object RemoteOppijanTunnistusService {

  def createCasClient(config: AppConfig): CasClient = {
    CasClientBuilder.buildFromConfigAndHttpClient(
    new CasConfig.CasConfigBuilder(
      config.settings.securitySettings.casVirkailijaUsername,
      config.settings.securitySettings.casVirkailijaPassword,
      config.settings.securitySettings.casUrl,
      OphUrlProperties.url("url-oppijan-tunnistus-service"),
      AppConfig.callerId,
      AppConfig.callerId,
      "/auth/cas")
      .setJsessionName("ring-session")
      .build(),
      SharedAsyncHttpClient.instance)
  }
}

class RemoteOppijanTunnistusService(casClient: CasClient) extends OppijanTunnistusService {
  implicit val formats = DefaultFormats

  def validateToken(token: String): Try[OppijantunnistusMetadata] = {
    val request = new RequestBuilder()
      .setMethod("GET")
      .setUrl(OphUrlProperties.url("oppijan-tunnistus.verify", token))
      .build()
    val future = toScala(casClient.execute(request))
    val result = future.map {
      case r if r.getStatusCode == 200 =>
        JsonMethods.parse(r.getResponseBodyAsStream()).extract[OppijanTunnistusVerification]
      case r => new RuntimeException(s"Error fetching oppijan-tunnistus. Token=$token, response code=${r.getStatusCode}")
    } // TODO retry .attemptRunFor(Duration(10, TimeUnit.SECONDS)
    val completedResult = Await.result(result, Duration(10, TimeUnit.SECONDS))
    Try(completedResult match {
      case OppijanTunnistusVerification(_, true, Some(metadata)) => metadata
      case OppijanTunnistusVerification(false, false, _) => throw new InvalidTokenException("invalid token")
      case OppijanTunnistusVerification(true, false, _) => throw new ExpiredTokenException("expired token")
      case _ => throw new InvalidTokenException("invalid token from oppijan tunnistus, no metadata")
    })
  }

}

class StubbedOppijanTunnistusService extends OppijanTunnistusService {
  override def validateToken(token: String): Try[OppijantunnistusMetadata] = token match {
    case hakemusId if hakemusId.startsWith("1.2.246.562.11.") => Success(OppijantunnistusMetadata(hakemusId, None, None))
    case "expiredToken" => Failure(new ExpiredTokenException("expired token"))
    case _ => Failure(new InvalidTokenException("invalid token"))
  }
}
