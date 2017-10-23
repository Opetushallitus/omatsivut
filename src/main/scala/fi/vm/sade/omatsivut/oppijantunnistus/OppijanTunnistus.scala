package fi.vm.sade.omatsivut.oppijantunnistus

import fi.vm.sade.omatsivut.NonSensitiveHakemusInfo.Oid
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.utils.http.{DefaultHttpClient, HttpClient}
import org.json4s._
import org.json4s.native.JsonMethods._

import scala.util.{Failure, Success, Try}

trait OppijanTunnistusComponent {
  val oppijanTunnistusService: OppijanTunnistusService
}

trait OppijanTunnistusService {
  def validateToken(token: String): Try[Oid]
}

case class OppijanTunnistusVerification(exists: Boolean, valid: Boolean, metadata: Option[HakuAppMetadata])

case class HakuAppMetadata(hakemusOid: Oid)

class InvalidTokenException(msg: String) extends RuntimeException(msg)

class ExpiredTokenException(msg: String) extends RuntimeException(msg)

class RemoteOppijanTunnistusService(client: HttpClient = DefaultHttpClient) extends OppijanTunnistusService {
  implicit val formats = DefaultFormats

  def validateToken(token: String): Try[Oid] = {

    val request = client.httpGet(OphUrlProperties.url("oppijan-tunnistus.verify", token))
                    .header("Caller-Id", "omatsivut.omatsivut.backend")

    request.responseWithHeaders() match {
      case (200, _, resultString) =>
        Try(parse(resultString, useBigDecimalForDouble = false).extract[OppijanTunnistusVerification]) match {
          case Success(OppijanTunnistusVerification(_, true, Some(HakuAppMetadata(hakemusOid)))) => Success(hakemusOid)
          case Success(OppijanTunnistusVerification(false, false, _)) => Failure(new InvalidTokenException("invalid token"))
          case Success(OppijanTunnistusVerification(true, false, _)) => Failure(new ExpiredTokenException("expired token"))
          case Success(_) => Failure(new InvalidTokenException("invalid token from oppijan tunnistus, no metadata"))
          case Failure(e) => Failure(new RuntimeException("invalid response from oppijan tunnistus", e))
        }
      case (code, _, resultString) =>
        Failure(new RuntimeException(s"Error fetching oppijan-tunnistus. Token=$token, response code=$code, content=$resultString"))
    }
  }

}

class StubbedOppijanTunnistusService extends OppijanTunnistusService {
  override def validateToken(token: String): Try[Oid] = token match {
    case hakemusId if hakemusId.startsWith("1.2.246.562.11.") => Success(hakemusId)
    case "expiredToken" => Failure(new ExpiredTokenException("expired token"))
    case _ => Failure(new InvalidTokenException("invalid token"))
  }
}
