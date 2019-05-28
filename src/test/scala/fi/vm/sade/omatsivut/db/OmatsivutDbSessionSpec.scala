package fi.vm.sade.omatsivut.db

import java.util.UUID

import fi.vm.sade.omatsivut.SessionFailure.SessionFailure
import fi.vm.sade.omatsivut.security.{Hetu, OppijaNumero, SessionId, SessionInfo}
import fi.vm.sade.omatsivut.{ITSetup, OmatsivutDbTools, SessionFailure}
import javax.security.sasl.AuthenticationException
import org.junit.runner.RunWith
import org.specs2.matcher.MustThrownExpectations
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope

@RunWith(classOf[JUnitRunner])
class OmatsivutDbSessionSpec extends Specification with ITSetup with OmatsivutDbTools {
  sequential

  step(appConfig.onStart)
  step(deleteAllSessions())

  "OmatsivutDb" should {
    "not find a non existing session" in {
      val notFoundSession = singleConnectionOmatsivutDb.get(SessionId(UUID.randomUUID()))
      notFoundSession must beLeft(SessionFailure.SESSION_NOT_FOUND)
    }

    "find a stored session" in new OneSessionInDatabase {
      val session = singleConnectionOmatsivutDb.get(id)
      session must beRight(SessionInfo(hetu, oppijaNumero, oppijaNimi))
    }

    "not find a stored session if timeout has expired" in new OneSessionInDatabase {
      val session = singleConnectionOmatsivutDb.get(id)
      setSessionLastAccessTime(id.value.toString, testSessionTimeout + 1)
      val sessionObsolete = singleConnectionOmatsivutDb.get(id)
      session must beRight(SessionInfo(hetu, oppijaNumero, oppijaNimi));
      sessionObsolete must beLeft(SessionFailure.SESSION_EXPIRED)
    }

    "delete a stored session by id" in new OneSessionInDatabase {
      singleConnectionOmatsivutDb.delete(id)
      val session = singleConnectionOmatsivutDb.get(id)
      session must beLeft(SessionFailure.SESSION_NOT_FOUND)
    }
  }

  step(deleteAllSessions())

  trait OneSessionInDatabase extends Scope with MustThrownExpectations {
    val hetu = Hetu("123456-789A")
    val oppijaNumero = OppijaNumero("1.2.3.4.5.6")
    val oppijaNimi = "John Smith"
    val id = singleConnectionOmatsivutDb.store(SessionInfo(hetu, oppijaNumero, oppijaNimi))
  }
}
