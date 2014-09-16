package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.ScalatraTestSupport
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.security.ShibbolethCookie

class SessionServletSpec extends ScalatraTestSupport {
  override lazy val appConfig = new AppConfig.IT
  addServlet(componentRegistry.newSecuredSessionServlet, "/secure")

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
}
