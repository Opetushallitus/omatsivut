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

    "is not constructed if cron string is not correct" in new MockSessionService {
      SessionCleaner.createTaskForScheduler(sessionService, "0 15 0 *") must throwA[IllegalArgumentException].like {
        case e => e.getMessage must contain("contains 4 parts but we expect one of [6]")
      }
    }

    "when executed will invoke the deleteAllExpired method" in new MockSessionService {
      val task = SessionCleaner.createTaskForScheduler(sessionService, "0 15 0 * * ?")

      task.execute(null, null)

      there was one(sessionService).deleteAllExpired()
    }
  }

  trait MockSessionService extends Mockito with Scope with MustThrownExpectations {
    val sessionService: SessionService = mock[SessionService]
  }
}
