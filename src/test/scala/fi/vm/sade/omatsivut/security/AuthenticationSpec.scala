package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.servlet.ApplicationsServlet
import fi.vm.sade.omatsivut.ScalatraTestSupport

class AuthenticationSpec extends ScalatraTestSupport {
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

  addServlet(new ApplicationsServlet(), "/*")
}
