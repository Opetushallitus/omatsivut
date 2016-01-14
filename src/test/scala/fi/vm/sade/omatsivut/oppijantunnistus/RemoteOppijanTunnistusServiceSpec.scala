package fi.vm.sade.omatsivut.oppijantunnistus

import fi.vm.sade.utils.http.{HttpClient, HttpRequest}
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import org.junit.runner.RunWith
import org.mockito.Matchers
import org.scalatra.test.specs2.MutableScalatraSpec
import org.specs2.mock.Mockito
import org.specs2.runner.JUnitRunner

import scala.util.Success

@RunWith(classOf[JUnitRunner])
class RemoteOppijanTunnistusServiceSpec extends MutableScalatraSpec with Mockito {

  val serviceUrl: String = "http://google.fi/url"
  val testToken: String = "testToken"
  val expectedHakemusOid: String = "expectedHakemusOid"

  "RemoteOppijanTunnistusService" should {

    "return hakemusOid from metadata on valid token" in {
      val client = mock[HttpClient]
      val request = mock[HttpRequest]

      val response = ("valid" -> true) ~ ("metadata" -> ("hakemusOid" -> expectedHakemusOid))

      request.header(Matchers.any[String], Matchers.any[String]).returns(request)
      request.responseWithHeaders().returns((200, Map(), compact(render(response))))
      client.httpGet(serviceUrl + "/" + testToken).returns(request)

      validateToken(testToken, client) should_== Success(expectedHakemusOid)
    }

    "return invalid token exception when token is not valid" in {
      val client = mock[HttpClient]
      val request = mock[HttpRequest]

      val response = "valid" -> false

      request.header(Matchers.any[String], Matchers.any[String]).returns(request)
      request.responseWithHeaders().returns((200, Map(), compact(render(response))))
      client.httpGet(serviceUrl + "/" + testToken).returns(request)

      validateToken(testToken, client) must beFailedTry.withThrowable[InvalidTokenException]
    }

    "returns RuntimeException if oppijan tunnistus does not respond 200" in {
      val client = mock[HttpClient]
      val request = mock[HttpRequest]

      request.header(Matchers.any[String], Matchers.any[String]).returns(request)
      request.responseWithHeaders().returns((500, Map(), ""))
      client.httpGet(serviceUrl + "/" + testToken).returns(request)

      validateToken(testToken, client) must beFailedTry.withThrowable[RuntimeException]
    }

  }

  def validateToken(token: String, httpClientMock: HttpClient) = new RemoteOppijanTunnistusService(serviceUrl, httpClientMock).validateToken(token)

}