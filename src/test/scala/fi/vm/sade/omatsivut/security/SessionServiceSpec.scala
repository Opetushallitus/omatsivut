package fi.vm.sade.omatsivut.security

import java.util.UUID

import fi.vm.sade.omatsivut.db.SessionRepository
import fi.vm.sade.utils.cas.{CasAuthenticatingClient, CasClient, CasParams}
import org.junit.runner.RunWith
import org.specs2.matcher.MustThrownExpectations
import org.specs2.mock.Mockito
import org.specs2.mock.mockito.MockitoStubs
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope
import scalaz.concurrent.Task


@RunWith(classOf[JUnitRunner])
class SessionServiceSpec extends Specification with MockitoStubs {

  "SessionService" should {

    "construct" in new SessionServiceWithMocks {
      sessionService must_!= null
    }

    "Authentication fails without credentials" in new SessionServiceWithMocks {
      sessionRepository.get(id) returns None
      sessionService.getSession(None) must beLeft.like { case t => t must beAnInstanceOf[AuthenticationFailedException]}
    }

    "Authentication fails if session not found in repository" in new SessionServiceWithMocks {
      sessionRepository.get(id) returns None
      sessionService.getSession(Some(id)) must beLeft.like { case t => t must beAnInstanceOf[AuthenticationFailedException] }
    }

    "Authentication succeeds if session is found in repository" in new SessionServiceWithMocks {
      sessionRepository.get(id) returns Some(session)
      sessionService.getSession(Some(id)) must_== Right(session)
    }

    "storeSession will persist a session in repository" in new SessionServiceWithMocks {
      sessionRepository.get(id) returns None
      sessionRepository.store(session) returns id
      sessionService.storeSession(oppijaNumero) must_== Right((id, session))
    }

    "deleteSession will delete a session from repository" in new SessionServiceWithMocks {
      sessionRepository.get(id) returns None
      sessionService.deleteSession(Some(id))
      there was one(sessionRepository).delete(id)
    }
  }

  trait SessionServiceWithMocks extends Mockito with Scope with MustThrownExpectations {
    val id = SessionId(UUID.randomUUID())
    val newId = SessionId(UUID.randomUUID())
    val oppijaNumero = OppijaNumero("123")
    val uid: String = "uid"
    val session = Session(oppijaNumero)
    val casClient: CasClient = mock[CasClient]

    val sessionRepository: SessionRepository = mock[SessionRepository]

    val sessionService = new SessionService(sessionRepository)
  }
}
