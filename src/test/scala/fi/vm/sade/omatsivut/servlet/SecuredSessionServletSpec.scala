package fi.vm.sade.omatsivut.servlet

import java.util.UUID

import fi.vm.sade.omatsivut.{ScalatraTestCookiesSupport, ScalatraTestSupport}
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.security.fake.FakeSAMLMessages
import fi.vm.sade.omatsivut.security.{CookieNames, SessionId}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SecuredSessionServletSpec extends ScalatraTestSupport with CookieNames with ScalatraTestCookiesSupport with FakeSAMLMessages {
  val originalUrl = "/foo/bar"
  val initsessionUrl = "initsession"
  val urlUsedByShibboleth = initsessionUrl + "?target=" + originalUrl

  "GET /initsession" should {

    "fails with bad request (400) if request does not contain body" in {
      post(urlUsedByShibboleth) {
        status must_== 400
        response.statusLine.message must contain("Invalid SOAP (SAML) message")
      }
    }

    "fails with bad request (400) if request does not contain henkilötunnus" in {
      post(urlUsedByShibboleth, invalidXMLBody()) {
        status must_== 400
        response.statusLine.message must contain("No hetu found in request from shibboleth")
      }
    }

    "creates a session in repository and forwards to root if the request body contains the SAML message with hetu, ignoring the 'target' parameter" in {
      deleteAllSessions
      post(urlUsedByShibboleth, requestBody(TestFixture.testHetu)) {
        status must_== 302
        val location = response.headers("Location")(0)
        location must endWith("omatsivut/index.html")
        val sessionId = cookieGetValue(response, sessionCookieName).getOrElse("not found session cookie")
        val personOid = getPersonFromSession(sessionId).getOrElse("not found in repository")
        personOid must_== TestFixture.personOid
        cookieGetValue(response, oppijaNumeroCookieName) must_== Some(TestFixture.personOid)
      }
    }
  }
}
