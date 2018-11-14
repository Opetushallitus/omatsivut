package fi.vm.sade.omatsivut.db

import java.util.UUID

import fi.vm.sade.omatsivut.security.{SessionId, OppijaNumero, Session}
import fi.vm.sade.omatsivut.{ITSetup, OmatsivutDbTools}
import org.junit.runner.RunWith
import org.specs2.matcher.MustThrownExpectations
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.specs2.specification.{BeforeAfterExample, Scope}

@RunWith(classOf[JUnitRunner])
class OmatsivutDbSessionSpec extends Specification with ITSetup with OmatsivutDbTools {
  sequential

  step(appConfig.onStart)
  step(deleteAllSessions())

  "OmatsivutDb" should {
    "not find a non existing session" in {
      val notFoundSession = singleConnectionOmatsivutDb.get(SessionId(UUID.randomUUID()))
      notFoundSession must beNone
    }

    "find a stored session" in new OneSessionInDatabase {
      val session = singleConnectionOmatsivutDb.get(id)
      session must not be none
      session.get must_== Session(oppijaNumero)
    }

    "delete a stored session by id" in new OneSessionInDatabase {
      singleConnectionOmatsivutDb.delete(id)
      val session = singleConnectionOmatsivutDb.get(id)
      session must beNone
    }
  }

  step(deleteAllSessions())

  trait OneSessionInDatabase extends Scope with MustThrownExpectations {
    val oppijaNumero = OppijaNumero("dummy")
    val id = singleConnectionOmatsivutDb.store(Session(oppijaNumero))
  }
}
