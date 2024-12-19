//package fi.vm.sade.omatsivut.security
//
//import java.util.UUID
//
//import fi.vm.sade.omatsivut.db.SessionRepository
//import fi.vm.sade.omatsivut.fixtures.TestFixture.testCASticket
//import org.junit.runner.RunWith
//import org.scalatra.Ok
//import org.scalatra.test.specs2.MutableScalatraSpec
//import org.specs2.mock.Mockito
//import org.specs2.runner.JUnitRunner
//
//@RunWith(classOf[JUnitRunner])
//class AuthenticationRequiringServletSpec extends MutableScalatraSpec with Mockito with AttributeNames {
//  sequential
//
//  val testUrl = "/test"
//  val id = SessionId(UUID.randomUUID())
//  val sessionRepository: SessionRepository = mock[SessionRepository]
//
//  def createTestSession(oppijaNumero: String): Map[String, String] = {
//    val hetu = "123456-789A"
//    val oppijaName = "John Smith"
//    val sessionData = SessionInfo(testCASticket, Hetu(hetu), OppijaNumero(oppijaNumero), oppijaName)
//    sessionRepository.get(id) returns Right(sessionData)
//    CookieHelper.cookieHeaderWith(sessionCookieName -> id.value.toString)
//  }
//
//  val dummyServlet = new AuthenticationRequiringServlet {
//    override implicit def sessionService: SessionService = new SessionService(sessionRepository)
//
//    get(testUrl) {
//      Ok("ok")
//    }
//  }
//
//  addServlet(dummyServlet, "/*")
//
//  "AuthenticationRequiringServlet" should {
//
//    "return authorization error if not authenticated" in {
//      get(testUrl) {
//        status must_== 401
//      }
//    }
//
//    "return bad request (400) if session cookie is not a correct UUID" in {
//      get(testUrl, headers = CookieHelper.cookieHeaderWith(sessionCookieName -> "NOT-AN-UUID")) {
//        status must_== 400
//      }
//    }
//
//    "let the servlet execute its route if authenticated" in {
//      get(testUrl, headers = createTestSession("1.2.3.4.5.6")) {
//        status must_== 200
//      }
//    }
//
//    "return 404 if session does not have person oid" in {
//      get(testUrl, headers = createTestSession("")) {
//        status must_== 404
//      }
//    }
//  }
//}
