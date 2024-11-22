//package fi.vm.sade.omatsivut.db
//
//import java.util.UUID
//
//import fi.vm.sade.omatsivut.SessionFailure.SessionFailure
//import fi.vm.sade.omatsivut.fixtures.TestFixture.testCASticket
//import fi.vm.sade.omatsivut.security.{Hetu, OppijaNumero, SessionId, SessionInfo}
//import fi.vm.sade.omatsivut.{ITSetup, OmatsivutDbTools, SessionFailure}
//import org.junit.runner.RunWith
//import org.specs2.matcher.MatchResult
//import org.specs2.mutable.Specification
//import org.specs2.runner.JUnitRunner
//import org.specs2.specification.Scope
//
//@RunWith(classOf[JUnitRunner])
//class OmatsivutDbSessionSpec extends Specification with ITSetup with OmatsivutDbTools {
//  sequential
//
//  step(appConfig.onStart)
//  step(deleteAllSessions())
//
//  "OmatsivutDb" should {
//    "not find a non existing session" in {
//      val notFoundSession = singleConnectionOmatsivutDb.get(SessionId(UUID.randomUUID()))
//      notFoundSession must beLeft(SessionFailure.SESSION_NOT_FOUND)
//    }
//
//    "find a stored session" in new OneSessionInDatabase {
//      val session = singleConnectionOmatsivutDb.get(id)
//      session must beRight(SessionInfo(testCASticket, hetu, oppijaNumero, oppijaNimi))
//    }
//
//    "not find a stored session if timeout has expired" in new OneSessionInDatabase {
//      val sessionBeforeExpiration = singleConnectionOmatsivutDb.get(id)
//      setSessionLastAccessTime(id.value.toString, testSessionTimeout + 1)
//      val sessionAfterExpiration = singleConnectionOmatsivutDb.get(id)
//      sessionBeforeExpiration must beRight(SessionInfo(testCASticket, hetu, oppijaNumero, oppijaNimi));
//      sessionAfterExpiration must beLeft(SessionFailure.SESSION_EXPIRED)
//    }
//
//    "delete all expired sessions does not delete anything if there is no expired sessions" in new ThreeSessionsInDatabase {
//      setSessionLastAccessTime(id1.value.toString, testSessionTimeout - 1)
//      setSessionLastAccessTime(id2.value.toString, testSessionTimeout - 5)
//      setSessionLastAccessTime(id3.value.toString, testSessionTimeout - 100)
//
//      val deletedCount = singleConnectionOmatsivutDb.deleteExpired()
//
//      deletedCount must beEqualTo(0)
//      verifySessionIsInDatabase(id1)
//      verifySessionIsInDatabase(id2)
//      verifySessionIsInDatabase(id3)
//    }
//
//    "delete all expired sessions" in new ThreeSessionsInDatabase {
//      setSessionLastAccessTime(id1.value.toString, testSessionTimeout + 1)
//      setSessionLastAccessTime(id2.value.toString, testSessionTimeout + 10000)
//      setSessionLastAccessTime(id3.value.toString, testSessionTimeout - 1)
//
//      val deletedCount = singleConnectionOmatsivutDb.deleteExpired()
//
//      deletedCount must beEqualTo(2)
//      verifySessionIsNotFoundInDatabase(id1)
//      verifySessionIsNotFoundInDatabase(id2)
//      verifySessionIsInDatabase(id3)
//    }
//
//    "delete a stored session by id" in new OneSessionInDatabase {
//      singleConnectionOmatsivutDb.delete(id)
//      verifySessionIsNotFoundInDatabase(id)
//    }
//  }
//
//  step(deleteAllSessions())
//
//  trait OneSessionInDatabase extends Scope with CommonsAndNonKeySessionData {
//    val id = createSessionAndVerifyItIsThere()
//  }
//
//  trait ThreeSessionsInDatabase extends Scope with CommonsAndNonKeySessionData {
//    val id1 = createSessionAndVerifyItIsThere()
//    val id2 = createSessionAndVerifyItIsThere()
//    val id3 = createSessionAndVerifyItIsThere()
//  }
//
//  trait CommonsAndNonKeySessionData {
//    val hetu = Hetu("123456-789A")
//    val oppijaNumero = OppijaNumero("1.2.3.4.5.6")
//    val oppijaNimi = "John Smith"
//
//    def createSessionAndVerifyItIsThere(): SessionId = {
//      val id: SessionId = singleConnectionOmatsivutDb.store(SessionInfo(testCASticket, hetu, oppijaNumero, oppijaNimi))
//      verifySessionIsInDatabase(id)
//      id
//    }
//
//    def verifySessionIsInDatabase(id: SessionId) = {
//      val existingSession = singleConnectionOmatsivutDb.get(id)
//      existingSession must beRight(SessionInfo(testCASticket, hetu, oppijaNumero, oppijaNimi))
//    }
//
//    def verifySessionIsNotFoundInDatabase(id: SessionId): MatchResult[Either[SessionFailure, SessionInfo]] = {
//      val nonExistingSession = singleConnectionOmatsivutDb.get(id)
//      nonExistingSession must beLeft(SessionFailure.SESSION_NOT_FOUND)
//    }
//  }
//}
