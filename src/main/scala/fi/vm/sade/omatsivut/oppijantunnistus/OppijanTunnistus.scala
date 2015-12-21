package fi.vm.sade.omatsivut.oppijantunnistus

import fi.vm.sade.omatsivut.HakemusOid.HakemusOid
import fi.vm.sade.utils.http.{DefaultHttpClient, HttpClient}
import org.json4s.native.JsonMethods._
import org.json4s._

import scala.util.{Failure, Success, Try}

trait OppijanTunnistusComponent {
  val oppijanTunnistusService: OppijanTunnistusService
}

trait OppijanTunnistusService {
  def validateToken(token: String): Try[HakemusOid]
}

case class OppijanTunnistusVerification(valid: Boolean, metadata: Option[HakuAppMetadata])

case class HakuAppMetadata(hakemusOid: HakemusOid)

class InvalidTokenException(msg: String) extends RuntimeException(msg)

class RemoteOppijanTunnistusService(verifyUrl: String, client: HttpClient = DefaultHttpClient) extends OppijanTunnistusService {
  implicit val formats = DefaultFormats

  def validateToken(token: String): Try[HakemusOid] = {

    val request = client.httpGet(verifyUrl + s"/$token")
                    .header("Caller-Id", "omatsivut.omatsivut.backend")

    request.responseWithHeaders() match {
      case (200, _, resultString) =>
        Try(parse(resultString).extract[OppijanTunnistusVerification]) match {
          case Success(OppijanTunnistusVerification(true, Some(HakuAppMetadata(hakemusOid)))) => Success(hakemusOid)
          case Success(OppijanTunnistusVerification(false, _)) => Failure(new InvalidTokenException("invalid token"))
          case Failure(e) => Failure(new RuntimeException("invalid response from oppijan tunnistus", e))
        }
      case (code, _, resultString) =>
        Failure(new RuntimeException(s"Error fetching oppijan-tunnistus. Token=$token, response code=$code, content=$resultString"))
    }
  }

}

class StubbedOppijanTunnistusService extends OppijanTunnistusService {
  override def validateToken(token: String): Try[HakemusOid] = Success("successHakemusOid")
}