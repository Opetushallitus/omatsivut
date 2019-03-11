package fi.vm.sade.omatsivut.security

import java.util.UUID

import fi.vm.sade.omatsivut.{PersonOid, ScalatraTestCookiesSupport, ScalatraTestSupport}
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.db.SessionRepository
import org.junit.runner.RunWith
import org.scalatra.{Ok, ScalatraServlet}
import org.scalatra.test.specs2.MutableScalatraSpec
import org.specs2.mock.Mockito
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AuthenticationRequiringServletSpec extends MutableScalatraSpec with Mockito with CookieNames {
  val testUrl = "/test"
  val id = SessionId(UUID.randomUUID())
  val sessionRepository: SessionRepository = mock[SessionRepository]

  val dummyServlet = new AuthenticationRequiringServlet {
    override val appConfig: AppConfig.AppConfig = new AppConfig.IT
    override implicit def sessionService: SessionService = new SessionService(sessionRepository)

    get(testUrl) {
      Ok("ok")
    }
  }

  addServlet(dummyServlet, "/*")

  "AuthenticationRequiringServlet" should {

    "return authorization error if not authenticated" in {
      val evo = 4
      get(testUrl) {
        status must_== 401
      }
    }

    "let the servlet execute its route if authenticated" in {
      val oppijaNumero = "123"
      val session = Session(OppijaNumero(oppijaNumero))
      sessionRepository.get(id) returns Some(session)
      val coo = CookieHelper.cookieHeaderWith(sessionCookieName -> id.value.toString)

      get(testUrl, headers = coo) {
        status must_== 200
      }
    }
  }
}
