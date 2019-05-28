package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.{PersonOid, ScalatraTestSupport}
import org.junit.runner.RunWith
import org.specs2.mutable.Before
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AuthenticationSpec extends ScalatraTestSupport {
  sequential

  implicit val personOid: PersonOid = PersonOid("1.2.246.562.24.14229104472")

  trait CleaningContext extends Before {
    def before: Any = deleteAllSessions()
  }

  "GET /applications (API)" should {

    "fails with 401 if not authenticated" in new CleaningContext {
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

  "GET / (UI)" should {

    "return 302 and redirect to shibboleh if not authenticated" in {
      get("") {
        status must_== 302
        val location = response.headers("Location")(0)
        location must beMatching(".*omatsivut.*initsession.*")
      }
    }

    "return 200 if proper session exists" in {
      authGet("") {
        status must_== 200
      }
    }
  }
}
