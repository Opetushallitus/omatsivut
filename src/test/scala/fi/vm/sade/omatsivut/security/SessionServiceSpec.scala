//package fi.vm.sade.omatsivut.security
//
//import fi.vm.sade.javautils.nio.cas.CasClient
//
//import java.util.UUID
//import fi.vm.sade.omatsivut.SessionFailure
//import fi.vm.sade.omatsivut.db.SessionRepository
//import fi.vm.sade.omatsivut.fixtures.TestFixture.testCASticket
//import org.junit.runner.RunWith
//import org.specs2.matcher.MustThrownExpectations
//import org.specs2.mock.Mockito
//import org.specs2.mock.mockito.MockitoStubs
//import org.specs2.mutable.Specification
//import org.specs2.runner.JUnitRunner
//import org.specs2.specification.Scope
//import scalaz.concurrent.Task
//
//
//@RunWith(classOf[JUnitRunner])
//class SessionServiceSpec extends Specification with MockitoStubs {
//
//  "SessionService" should {
//
//    "construct" in new SessionServiceWithMocks {
//      sessionService must_!= null
//    }
//
//    "Authentication fails without credentials" in new SessionServiceWithMocks {
//      sessionRepository.get(id) returns Left(SessionFailure.SESSION_NOT_FOUND)
//      sessionService.getSession(None) must beLeft.like { case t => t must beAnInstanceOf[AuthenticationFailedException]}
//    }
//
//    "Authentication fails if session not found in repository" in new SessionServiceWithMocks {
//      sessionRepository.get(id) returns Left(SessionFailure.SESSION_NOT_FOUND)
//      sessionService.getSession(Some(id)) must beLeft.like { case t => t must beAnInstanceOf[AuthenticationFailedException] }
//    }
//
//    "Authentication fails if session is found but expired" in new SessionServiceWithMocks {
//      sessionRepository.get(id) returns Left(SessionFailure.SESSION_EXPIRED)
//      sessionService.getSession(Some(id)) must beLeft.like { case t => t must beAnInstanceOf[AuthenticationFailedException] }
//    }
//
//    "Authentication succeeds if session is found in repository" in new SessionServiceWithMocks {
//      sessionRepository.get(id) returns Right(session)
//      sessionService.getSession(Some(id)) must_== Right(session)
//    }
//
//    "storeSession will persist a session in repository" in new SessionServiceWithMocks {
//      sessionRepository.get(id) returns Left(SessionFailure.SESSION_NOT_FOUND)
//      sessionRepository.store(session) returns id
//      sessionService.storeSession(testCASticket, hetu, oppijaNumero, oppijaNimi) must_== Right((id, session))
//    }
//
//    "deleteSession will delete a session from repository" in new SessionServiceWithMocks {
//      sessionService.deleteSession(Some(id))
//      there was one(sessionRepository).delete(id)
//    }
//
//    "deleteAllExpiredSessions will delete expired sessions" in new SessionServiceWithMocks {
//      sessionService.deleteAllExpired()
//      there was one(sessionRepository).deleteExpired()
//    }
//  }
//
//  trait SessionServiceWithMocks extends Mockito with Scope with MustThrownExpectations {
//    val id = SessionId(UUID.randomUUID())
//    val newId = SessionId(UUID.randomUUID())
//    val hetu = Hetu("123456-789A")
//    val oppijaNumero = OppijaNumero("1.2.3.4.5.6")
//    val oppijaNimi = "John Smith"
//    val uid: String = "uid"
//    val session = SessionInfo(testCASticket, hetu, oppijaNumero, oppijaNimi)
//    val casClient: CasClient = mock[CasClient]
//
//    val sessionRepository: SessionRepository = mock[SessionRepository]
//
//    val sessionService = new SessionService(sessionRepository)
//  }
//}
