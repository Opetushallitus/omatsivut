package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.ScalatraTestSupport
import fi.vm.sade.omatsivut.hakemus.FixturePerson

class AuthenticationSpec extends ScalatraTestSupport with FixturePerson {
  override lazy val appConfig = new AppConfig.IT
  addServlet(componentRegistry.newApplicationsServlet, "/api/applications")

  "GET /applications" should {
    "return 401 if not authenticated" in {
      get("/api/applications") {
        status must_== 401
      }
    }

    "return 200 with proper credentials" in {
      authGet("/api/applications") {
        status must_== 200
      }
    }
  }
}
