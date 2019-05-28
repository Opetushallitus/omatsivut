package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.security.{AttributeNames, SessionId, OppijaNumero}
import fi.vm.sade.omatsivut.{PersonOid, ScalatraTestCookiesSupport, ScalatraTestSupport}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LogoutServletSpec extends ScalatraTestSupport with AttributeNames with ScalatraTestCookiesSupport {

  implicit val personOid: PersonOid = PersonOid("dummy")

  "logout" should {

    "clear the session and oppijaNumero cookies and deletes the session from repository, redirect to oma-opintopolku" in {
      authGet("logout?koski=true") {
        status must_== 302
        val location = response.headers("Location")(0)
        location must endWith("Shibboleth.sso/Logout?return=%2Foma-opintopolku")
        getPersonFromSession(lastSessionId) must beNone
        cookieGetValue(response, sessionCookieName) must beNone
      }
    }

    "redirect to koski logout if koski parameter is not given" in {
      authGet("logout") {
        status must_== 302
        val location = response.headers("Location")(0)
        location must endWith("Shibboleth.sso/Logout?return=%2Fkoski%2Fuser%2Flogout")
        getPersonFromSession(lastSessionId) must beNone
        cookieGetValue(response, sessionCookieName) must beNone
      }
    }
  }
}
