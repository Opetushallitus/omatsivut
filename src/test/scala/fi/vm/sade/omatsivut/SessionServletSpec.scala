package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.security.ShibbolethCookie
import fi.vm.sade.omatsivut.servlet.session.SecuredSessionServlet
import org.scalatra.test.specs2.MutableScalatraSpec

class SessionServletSpec extends MutableScalatraSpec {
  "GET /secure/initsession" should {
    "generate auth cookie" in {
      val shibbolethCookie: ShibbolethCookie = ShibbolethCookie("_shibsession_test", "test")
      get("/secure/initsession", headers = Map("Hetu" -> TestFixture.testHetu, "Cookie" -> shibbolethCookie.toString)) {
        status must_== 302
        val setCookie = response.headers("Set-Cookie")(0)
        val encrypted = setCookie.substring(setCookie.indexOf('='), setCookie.indexOf(';') + 1)
        encrypted must_!= ""
      }
    }

    "redirect to Shibboleth login" in {
      get("/secure/initsession") {
        status must_== 302
        val location = response.headers("Location")(0)
        location must endWith("/omatsivut/Shibboleth.sso/LoginFI")
      }
    }
  }

  addServlet(new SecuredSessionServlet()(new AppConfig.IT), "/secure")
}
