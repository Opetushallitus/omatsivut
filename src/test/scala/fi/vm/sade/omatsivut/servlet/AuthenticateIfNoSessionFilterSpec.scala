package fi.vm.sade.omatsivut.servlet

import java.util.UUID

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.omatsivut.{ITSetup, SessionFailure}
import fi.vm.sade.omatsivut.db.SessionRepository
import fi.vm.sade.omatsivut.security._
import org.junit.runner.RunWith
import org.scalatra.{Ok, ScalatraServlet}
import org.scalatra.test.specs2.MutableScalatraSpec
import org.specs2.matcher.{AnyMatchers, Matchers, MustThrownExpectations}
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
  var sessionInfoFromSession: Option[SessionInfo] = None

  addFilter(authenticateIfNoSessionFilter, "/*")

  val dummyServlet = new ScalatraServlet {
    get(originalUrl) {
      val sessionInfo = request.getSession().getAttribute("sessionInfo").asInstanceOf[SessionInfo]
      sessionInfoFromSession = if (sessionInfo != null) Some(sessionInfo) else None
      Ok("ok")
    }
  }
  addServlet(dummyServlet, "/*")

  sequential

  "AuthenticateIfNoSessionFilter" should {

    "redirect to login if session does not exist in cookie" in {
      sessionRepository.get(id) returns Left(SessionFailure.SESSION_NOT_FOUND)
      get("omatsivut" + originalUrl) {
        status must_== 302
        val location = response.headers("Location")(0)
        location must find("""/omatsivut/initsession/\?lang=fi$""")
      }
    }

    "redirect to login if session exists in cookie but not in repository" in {
      sessionRepository.get(id) returns Left(SessionFailure.SESSION_NOT_FOUND)
      get(originalUrl, headers = CookieHelper.cookieHeaderWith("session" -> id.value.toString)) {
        status must_== 302
      }
    }

    "pass if session exists in cookie and in repository, and create a copy in http request's session" in new WithTestSession {
      sessionInfoFromSession = None
      get(originalUrl, headers = CookieHelper.cookieHeaderWith("session" -> id.value.toString)) {
        status must_== 200
        sessionInfoFromSession must_!= None
        sessionInfoFromSession.map(_.hetu) must_== Some(hetu)
      }
    }
  }

  trait WithTestSession extends Scope with MustThrownExpectations {
    val hetu = Hetu("123456-789A")
    val oppijaNumero = OppijaNumero("1.2.3.4.5.6")
    val oppijaNimi = "John Smith"
    val testSession = SessionInfo(hetu, oppijaNumero, oppijaNimi)
    sessionRepository.get(id) returns Right(testSession)
  }
}


