package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.security.{CookieNames, SessionId, OppijaNumero}
import fi.vm.sade.omatsivut.{PersonOid, ScalatraTestCookiesSupport, ScalatraTestSupport}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LogoutServletSpec extends ScalatraTestSupport with CookieNames with ScalatraTestCookiesSupport {

  implicit val personOid: PersonOid = PersonOid("dummy")

  "logout" should {

    "clear the session and oppijaNumero cookies and deletes the session from repository" in {
      authGet("logout") {
        status must_== 302
        val location = response.headers("Location")(0)
        location must endWith("omatsivut/shibboleth/Logout?return=%2Foma-opintopolku")
        getPersonFromSession(lastSessionId) must beNone
        cookieGetValue(response, sessionCookieName) must beNone
        cookieGetValue(response, oppijaNumeroCookieName) must beNone
      }
    }
  }
}
