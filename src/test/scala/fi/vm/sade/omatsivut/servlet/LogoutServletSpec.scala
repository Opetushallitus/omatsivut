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
        location must endWith("Shibboleth.sso/Logout?return=%2Foma-opintopolku")
        getPersonFromSession(sessionId) must beNone
        cookieGetValue(response, sessionCookieName) must beNone
      }
    }

    "redirect to koski logout if koski parameter is not given" in {
      authGetAndReturnSession("logout") { sessionId =>
        status must_== 302
        val location = response.headers("Location")(0)
        location must endWith("Shibboleth.sso/Logout?return=%2Fkoski%2Fuser%2Flogout")
        getPersonFromSession(sessionId) must beNone
        cookieGetValue(response, sessionCookieName) must beNone
      }
    }
  }
}
