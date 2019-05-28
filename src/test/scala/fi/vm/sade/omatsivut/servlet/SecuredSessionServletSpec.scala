package fi.vm.sade.omatsivut.servlet

import java.util.UUID

import fi.vm.sade.omatsivut.{ScalatraTestCookiesSupport, ScalatraTestSupport}
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.security.{AttributeNames}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SecuredSessionServletSpec extends ScalatraTestSupport with AttributeNames with ScalatraTestCookiesSupport {
  val urlUsedByShibboleth = "initsession"

  "GET /initsession" should {
    "fails with bad request (400) if request does not contain henkilötunnus" in {
      get(urlUsedByShibboleth) {
        status must_== 400
        response.statusLine.message must contain("No hetu found in request from shibboleth")
      }
    }

    "create a session in repository and forwards to root if the request contains hetu header" in {
      deleteAllSessions
      get(urlUsedByShibboleth, headers = Map("hetu" -> TestFixture.testHetu)) {
        status must_== 302
        val location = response.headers("Location")(0)
        location must endWith("omatsivut/index.html")
        val sessionId = cookieGetValue(response, sessionCookieName).getOrElse("not found session cookie")
        val personOid = getPersonFromSession(sessionId).getOrElse("not found in repository")
        personOid must_== TestFixture.personOid
      }
    }

    "create a session with no oid if hetu does not have the corresponding oid" in {
      deleteAllSessions
      get(urlUsedByShibboleth, headers = Map("hetu" -> TestFixture.testHetuWithNoPersonOid)) {
        status must_== 302
        val location = response.headers("Location")(0)
        location must endWith("omatsivut/index.html")
        val sessionId = cookieGetValue(response, sessionCookieName).getOrElse("not found session cookie")
        val personOid = getPersonFromSession(sessionId).getOrElse("not found in repository")
        personOid must_== ""
      }
    }

    "create a session in repository, and it will contain also the display name of the user" in {
      deleteAllSessions
      val firstName = "Wolfgang"
      val secondName = "Mozart"
      get(urlUsedByShibboleth, headers = Map("hetu" -> TestFixture.testHetu, "firstname" -> firstName, "sn" -> secondName)) {
        status must_== 302
        val sessionId = cookieGetValue(response, sessionCookieName).getOrElse("not found session cookie")
        val displayName = getDisplayNameFromSession(sessionId).getOrElse("not found in repository")
        displayName must_== firstName + " " + secondName
      }
    }
  }
}
