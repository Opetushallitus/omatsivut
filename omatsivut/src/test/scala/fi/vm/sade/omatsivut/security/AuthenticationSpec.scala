package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.ScalatraTestSupport
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.hakemus.FixturePerson
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AuthenticationSpec extends ScalatraTestSupport with FixturePerson {

  "GET /applications" should {
    "return 401 if not authenticated" in {
      get("secure/applications") {
        status must_== 401
      }
    }

    "return 200 with proper credentials" in {
      authGet("secure/applications") {
        status must_== 200
      }
    }
  }
}
