package fi.vm.sade.omatsivut.oppijantunnistus

import java.util.concurrent.TimeUnit

import fi.vm.sade.ataru.AtaruApplication
import fi.vm.sade.omatsivut.NonSensitiveHakemusInfo.Oid
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.utils.cas.{CasAuthenticatingClient, CasClient, CasParams}
import fi.vm.sade.utils.http.{DefaultHttpClient, HttpClient}
import org.http4s.Method.GET
import org.http4s.{Request, Uri, client}
import org.http4s.client.blaze
import org.json4s._
import org.json4s.jackson.JsonMethods
import org.json4s.jackson.JsonMethods._
import scalaz.concurrent.Task

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

class RemoteOppijanTunnistusService(config: AppConfig) extends OppijanTunnistusService {
  implicit val formats = DefaultFormats

  private val blazeHttpClient = blaze.defaultClient
  private val casClient = new CasClient(
    config.settings.securitySettings.casUrl,
    blazeHttpClient,
    AppConfig.callerId
  )
  private val casParams = CasParams(
    OphUrlProperties.url("url-oppijan-tunnistus-service"),
    "auth/cas",
    config.settings.securitySettings.casUsername,
    config.settings.securitySettings.casPassword
  )
  private val httpClient = CasAuthenticatingClient(
    casClient,
    casParams,
    blazeHttpClient,
    AppConfig.callerId,
    "ring-session"
  )

  def validateToken(token: String): Try[OppijantunnistusMetadata] = {
    Try(Uri.fromString(OphUrlProperties.url("oppijan-tunnistus.verify", token))
      .fold(Task.fail, uri => {
        httpClient.fetch(Request(method = GET, uri = uri)) {
          case r if r.status.code == 200 =>
            r.as[String].map(s => JsonMethods.parse(s).extract[OppijanTunnistusVerification])
          case r => Task.fail(new RuntimeException(s"Error fetching oppijan-tunnistus. Token=$token, response code=${r.status.code}"))
        }
      }).attemptRunFor(Duration(10, TimeUnit.SECONDS)).fold(throw _, x => x) match {
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
