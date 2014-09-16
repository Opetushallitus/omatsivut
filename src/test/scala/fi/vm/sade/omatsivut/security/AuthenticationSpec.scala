package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.ScalatraTestSupport

class AuthenticationSpec extends ScalatraTestSupport {
  override lazy val appConfig = new AppConfig.IT
  addServlet(appConfig.componentRegistry.newApplicationsServlet, "/*")

  "GET /applications" should {
    "return 401 if not authenticated" in {
      get("/applications") {
        status must_== 401
      }
    }

    "return 200 with proper credentials" in {
      authGet("/applications", TestFixture.personOid) {
        status must_== 200
      }
    }
  }
}
