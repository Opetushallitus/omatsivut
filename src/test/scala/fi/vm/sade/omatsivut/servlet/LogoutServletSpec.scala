package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.security.AttributeNames
import fi.vm.sade.omatsivut.{PersonOid, ScalatraTestCookiesSupport, ScalatraTestSupport}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LogoutServletSpec extends ScalatraTestSupport with AttributeNames with ScalatraTestCookiesSupport {

  implicit val personOid: PersonOid = PersonOid("dummy")

  "logout" should {

    "clear the session and oppijaNumero cookies and deletes the session from repository, redirect to oma-opintopolku" in {
      authGetAndReturnSession("logout?koski=true") { sessionId =>
        status must_== 302
        val location = response.headers("Location")(0)
        // this regex reads as "full URL to CAS-Oppija logout endpoint with full URL of oma-opintopolku as return URL"
        location must find("""^http://.*/cas-oppija/logout\?service=http(s)?%3A%2F%2F.*%2Foma-opintopolku$""")
        getPersonFromSession(sessionId) must beNone
        cookieGetValue(response, sessionCookieName) must beNone
      }
    }
  }
}
