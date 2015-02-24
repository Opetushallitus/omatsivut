package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.ScalatraTestSupport
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.security.ShibbolethCookie
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SessionServletSpec extends ScalatraTestSupport {
  override lazy val appConfig = new AppConfig.IT

  "GET /secure/initsession" should {
    "redirect to Shibboleth login" in {
      get("secure/initsession") {
        status must_== 302
        val location = response.headers("Location")(0)
        location must endWith("/omatsivut/Shibboleth.sso/LoginFI")
      }
    }
  }
}
