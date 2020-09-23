package fi.vm.sade.omatsivut.servlet

import java.util.UUID

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.db.SessionRepository
import fi.vm.sade.omatsivut.security._
import fi.vm.sade.omatsivut.{ITSetup, SessionFailure}
import org.junit.runner.RunWith
import org.scalatra.test.specs2.MutableScalatraSpec
import org.scalatra.{Ok, ScalatraServlet}
import org.specs2.mock.Mockito
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope

@RunWith(classOf[JUnitRunner])
class AuthenticateIfNoSessionFilterSpec extends MutableScalatraSpec with Mockito with ITSetup {
  val originalUrl = "/index.html"
  implicit val language: Language.Language = Language.fi
  val id = SessionId(UUID.randomUUID())
  val sessionRepository: SessionRepository = mock[SessionRepository]
  val sessionService = new SessionService(sessionRepository)
  val authenticateIfNoSessionFilter = new AuthenticateIfNoSessionFilter(sessionService)

  addFilter(authenticateIfNoSessionFilter, "/*")

  val dummyServlet = new ScalatraServlet {
    get(originalUrl) {
      Ok("ok")
    }
  }
  addServlet(dummyServlet, "/*")

  sequential

  "AuthenticateIfNoSessionFilter" should {

    "redirect to login if session does not exist in cookie" in new NoSessionInDatabase {
      get("omatsivut" + originalUrl) {
        status must_== 302
        val location = response.headers("Location")(0)
        // this regex reads as "full URL to CAS-Oppija login endpoint with full URL of service as return URL"
        location must find("""^http://.*/cas-oppija/login\?valtuudet=""" + AppConfig.suomifi_valtuudet_enabled +  """&service=http(s)?%3A%2F%2F.*%2Fomatsivut%2Finitsession$""")
      }
    }

    "redirect to login if correctly formatted session exists in cookie, but not in repository" in new NoSessionInDatabase {
      get(originalUrl, headers = CookieHelper.cookieHeaderWith("session" -> id.value.toString)) {
        status must_== 302
      }
    }

    "return BadRequest (400) if session exists in cookie, but is not a correct UUID" in {
      get(originalUrl, headers = CookieHelper.cookieHeaderWith("session" -> "NOT-AN-UUID")) {
        status must_== 400
      }
    }

    "pass if session exists in cookie and in repository" in new WithTestSession {
      get(originalUrl, headers = CookieHelper.cookieHeaderWith("session" -> id.value.toString)) {
        status must_== 200
      }
    }
  }

  trait NoSessionInDatabase extends Scope {
    sessionRepository.get(id) returns Left(SessionFailure.SESSION_NOT_FOUND)
  }

  trait WithTestSession extends Scope {
    val hetu = Hetu("123456-789A")
    val oppijaNumero = OppijaNumero("1.2.3.4.5.6")
    val oppijaNimi = "John Smith"
    val testSession = SessionInfo(hetu, oppijaNumero, oppijaNimi)
    sessionRepository.get(id) returns Right(testSession)
  }
}


