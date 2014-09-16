package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.ScalatraTestSupport
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.fixtures.TestFixture

class AuthenticationTimeOutSpec extends ScalatraTestSupport {
  override lazy val appConfig = new AppConfig.ImmediateCookieTimeout
  addServlet(appConfig.componentRegistry.newApplicationsServlet, "/*")

  "GET /applications" should {
    "return 401 if cookie has timed out" in {
      authGet("/applications", TestFixture.personOid) {
        status must_== 401
      }
    }

    "delete cookie if cookie has timed out" in {
      authGet("/applications", TestFixture.personOid) {
        val cookieValues = response.getHeader("Set-Cookie").split(";").toList
        val expires = cookieValues.find(_.startsWith("Expires="))
        expires.get must_== "Expires=Thu, 01-Jan-1970 00:00:00 GMT"
        val path = cookieValues.find(_.startsWith("Path="))
        path.get must_== "Path=/"
      }
    }
  }
}
