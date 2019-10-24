package fi.vm.sade.omatsivut.oppijantunnistus

import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.utils.http.{HttpClient, HttpRequest}
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.junit.runner.RunWith
import org.mockito.{ArgumentMatchers, Matchers}
import org.scalatra.test.specs2.MutableScalatraSpec
import org.specs2.mock.Mockito
import org.specs2.runner.JUnitRunner

import scala.util.{Success, Try}

@RunWith(classOf[JUnitRunner])
class RemoteOppijanTunnistusServiceSpec extends MutableScalatraSpec with Mockito {

  val testToken: String = "testToken"
  val url: String = OphUrlProperties.url("oppijan-tunnistus.verify", testToken)
  val expectedHakemusOid: String = "expectedHakemusOid"

  "RemoteOppijanTunnistusService" should {

    "return hakemusOid from metadata on valid token" in {
      val client = mock[HttpClient]
      val request = mock[HttpRequest]

      val response = ("exists" -> true) ~ ("valid" -> true) ~ ("metadata" -> ("hakemusOid" -> expectedHakemusOid))

      request.header(ArgumentMatchers.any[String], ArgumentMatchers.any[String]).returns(request)
      request.responseWithHeaders().returns((200, Map(), compact(render(response))))
      println(url)
      client.httpGet(url).returns(request)

      validateToken(testToken, client) should_== Success(OppijantunnistusMetadata(expectedHakemusOid, None, None))
    }

    "return invalid token exception when token is not valid" in {
      val client = mock[HttpClient]
      val request = mock[HttpRequest]

      val response = ("exists" -> false) ~ ("valid" -> false)

      request.header(ArgumentMatchers.any[String], ArgumentMatchers.any[String]).returns(request)
      request.responseWithHeaders().returns((200, Map(), compact(render(response))))
      client.httpGet(url).returns(request)

      validateToken(testToken, client) must beFailedTry.withThrowable[InvalidTokenException]
    }

    "return expired token exception when token has expired" in {
      val client = mock[HttpClient]
      val request = mock[HttpRequest]

      val response = ("exists" -> true) ~ ("valid" -> false)

      request.header(ArgumentMatchers.any[String], ArgumentMatchers.any[String]).returns(request)
      request.responseWithHeaders().returns((200, Map(), compact(render(response))))
      client.httpGet(url).returns(request)

      validateToken(testToken, client) must beFailedTry.withThrowable[ExpiredTokenException]
    }

    "returns RuntimeException if oppijan tunnistus does not respond 200" in {
      val client = mock[HttpClient]
      val request = mock[HttpRequest]

      request.header(ArgumentMatchers.any[String], ArgumentMatchers.any[String]).returns(request)
      request.responseWithHeaders().returns((500, Map(), ""))
      client.httpGet(url).returns(request)

      validateToken(testToken, client) must beFailedTry.withThrowable[RuntimeException]
    }

  }

  def validateToken(token: String, httpClientMock: HttpClient): Try[OppijantunnistusMetadata] =
    new RemoteOppijanTunnistusService(httpClientMock).validateToken(token)

}
