package fi.vm.sade.omatsivut.security

import org.junit.runner.RunWith
import org.specs2.matcher.MustThrownExpectations
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope


@RunWith(classOf[JUnitRunner])
class SessionCleanerSpec extends Specification {

  "SessionCleaner task" should {

    "is constructed" in new SessionCleanerWithMocks {
      task must_!= null
    }

    "when executed will invoke the deleteAllExpired method" in new SessionCleanerWithMocks {

      task.execute(null, null)

      there was one(sessionService).deleteAllExpired()
    }
  }

  trait SessionCleanerWithMocks extends Mockito with Scope with MustThrownExpectations {
    val sessionService: SessionService = mock[SessionService]
    val task = SessionCleaner.createTaskForScheduler(sessionService, "0 15 0 * * ?")
  }
}
