package fi.vm.sade.omatsivut.oppijantunnistus

import fi.vm.sade.groupemailer.Json4sHttp4s
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.utils.http.{HttpClient, HttpRequest}
import org.http4s.{EmptyBody, Request, Response, Service, Status, Uri}
import org.http4s.client.{Client, DisposableResponse, blaze}
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.junit.runner.RunWith
import org.mockito.{ArgumentMatchers, Matchers}
import org.scalatra.test.specs2.MutableScalatraSpec
import org.specs2.mock.Mockito
import org.specs2.runner.JUnitRunner

import scala.util.{Success, Try}
import org.http4s.Method.GET
import org.json4s.DefaultFormats
import org.scalatra.{Created, Ok}
import scalaz.concurrent.Task

@RunWith(classOf[JUnitRunner])
class RemoteOppijanTunnistusServiceSpec extends MutableScalatraSpec with Mockito {
  implicit val appConfig = new AppConfig.IT
  implicit val formats = DefaultFormats

  val testToken: String = "testToken"
  val url: String = OphUrlProperties.url("oppijan-tunnistus.verify", testToken)
  val expectedHakemusOid: String = "expectedHakemusOid"

  def clientWithResponse(r: Response) = {
    def open(req: Request): Task[DisposableResponse] = {
      Task.now(DisposableResponse(response = r, dispose = Task.now()))
    }
    Client(open = Service.lift(open), shutdown = Task.now())
  }

  "RemoteOppijanTunnistusService" should {

    "return hakemusOid from metadata on valid token" in {

      val response = ("exists" -> true) ~ ("valid" -> true) ~ ("metadata" -> ("hakemusOid" -> expectedHakemusOid))
      val client = clientWithResponse(Response(
        status= Status.Ok
      ).withBody(response)(Json4sHttp4s.json4sEncoderOf).unsafePerformSync)

      validateToken(testToken, client) should_== Success(OppijantunnistusMetadata(expectedHakemusOid, None, None))
    }


    "return invalid token exception when token is not valid" in {
      val response = ("exists" -> false) ~ ("valid" -> false)
      val client = clientWithResponse(Response(
        status= Status.Ok
      ).withBody(response)(Json4sHttp4s.json4sEncoderOf).unsafePerformSync)

      validateToken(testToken, client) must beFailedTry.withThrowable[InvalidTokenException]
    }

    "return expired token exception when token has expired" in {
      val response = ("exists" -> true) ~ ("valid" -> false)
      val client = clientWithResponse(Response(
        status= Status.Ok
      ).withBody(response)(Json4sHttp4s.json4sEncoderOf).unsafePerformSync)

      validateToken(testToken, client) must beFailedTry.withThrowable[ExpiredTokenException]
    }

    "returns RuntimeException if oppijan tunnistus does not respond 200" in {
      val client = clientWithResponse(Response(status= Status.InternalServerError))
      validateToken(testToken, client) must beFailedTry.withThrowable[RuntimeException]
    }

  }

  def validateToken(token: String, httpClientMock: Client): Try[OppijantunnistusMetadata] =
    new RemoteOppijanTunnistusService(httpClientMock).validateToken(token)

}
