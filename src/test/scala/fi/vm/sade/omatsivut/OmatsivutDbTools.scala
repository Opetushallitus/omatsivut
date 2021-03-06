package fi.vm.sade.omatsivut

import java.util.UUID
import java.util.concurrent.TimeUnit

import fi.vm.sade.omatsivut.db.impl.OmatsivutDb
import fi.vm.sade.omatsivut.fixtures.TestFixture.testCASticket
import fi.vm.sade.omatsivut.security.{Hetu, OppijaNumero, SessionId, SessionInfo}
import org.slf4j.LoggerFactory
import org.specs2.mutable.Specification
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.duration.Duration


trait OmatsivutDbTools extends Specification {
  private val logger = LoggerFactory.getLogger(classOf[OmatsivutDbTools])

  val singleConnectionOmatsivutDb: OmatsivutDb

  def createTestSession()(implicit personOid: PersonOid): SessionId = {
    val dummyHetu = Hetu("121212-789A")
    val dummyName = "John Smith"
    singleConnectionOmatsivutDb.store(SessionInfo(testCASticket, dummyHetu, OppijaNumero(personOid.oid), dummyName))
  }

  def setSessionLastAccessTime(sessionIdString: String, howManySecondsFromNow: Int): Unit = {
    singleConnectionOmatsivutDb.runBlocking(DBIO.seq(
      sqlu"""update sessions
                        set viimeksi_luettu = now() - interval '#${howManySecondsFromNow} seconds'
                        where id = ${sessionIdString}::uuid"""
    ).transactionally, Duration(20, TimeUnit.SECONDS))
  }

  def setPersonIdToEmptyBySessionId(sessionIdString: String): Unit = {
    singleConnectionOmatsivutDb.runBlocking(DBIO.seq(
      sqlu"""update sessions
                        set oppija_numero = ''
                        where id = ${sessionIdString}::uuid"""
    ).transactionally, Duration(20, TimeUnit.SECONDS))
  }

  def getPersonFromSession(sessionId: String): Option[String] = getPersonFromSession(SessionId(UUID.fromString(sessionId)))

  def getPersonFromSession(sessionId: SessionId): Option[String] = {
    logger.info(s"sessionId: $sessionId")
    singleConnectionOmatsivutDb.get(sessionId) match {
      case x@Right(SessionInfo(_, _, oppijaNumero, _)) =>
        logger.info(s"Found from db: $x")
        Some(oppijaNumero.value)
      case x =>
        logger.info(s"Problem when getting session $sessionId from db: got $x")
        None
    }
  }

  def getDisplayNameFromSession(sessionIdString: String): Option[String] = {
    val sessionId = SessionId(UUID.fromString(sessionIdString))
    singleConnectionOmatsivutDb.get(sessionId) match {
      case Right(SessionInfo(_, _, _, oppijaNimi)) => Some(oppijaNimi)
      case _ => None
    }
  }

  def deleteAllSessions(): Unit = {
    logger.info("Starting to delete all sessions...")
    singleConnectionOmatsivutDb.runBlocking(DBIO.seq(
      sqlu"truncate table sessions cascade"
      ).transactionally)
    logger.info("...Finished deleting all sessions.")
  }
}
