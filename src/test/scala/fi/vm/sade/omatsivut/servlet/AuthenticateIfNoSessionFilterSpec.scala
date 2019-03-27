package fi.vm.sade.omatsivut.servlet

import java.util.UUID

import fi.vm.sade.hakemuseditori.domain.Language
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
class AuthenticateIfNoSessionFilterSpec extends MutableScalatraSpec with Mockito {
  val originalUrl = "/index.html"
  val redirectedUrl = "/shib/omatsivut/initsession?target=" + originalUrl
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
  addServlet(dummyServlet, "/omatsivut/*")

  sequential

  "AuthenticateIfNoSessionFilter" should {

    "redirect to login if session does not exist in cookie" in {
      sessionRepository.get(id) returns None
      get("omatsivut" + originalUrl) {
        status must_== 302
        val location = response.headers("Location")(0)
        location must find("""Shibboleth.sso\/LoginFI\?target=http.+%2Fomatsivut%2Finitsession%2F$""")
      }
    }

    "redirect to login if session exists in cookie but not in repository" in {
      sessionRepository.get(id) returns None
      get(originalUrl, headers = CookieHelper.cookieHeaderWith("session" -> id.value.toString)) {
        status must_== 302
      }
    }

    "pass if session exists in cookie and in repository" in {
      val session = Session(OppijaNumero("123"))
      sessionRepository.get(id) returns Some(session)

      get(originalUrl, headers = CookieHelper.cookieHeaderWith("session" -> id.value.toString)) {
        status must_== 200
      }
    }

  }
}

