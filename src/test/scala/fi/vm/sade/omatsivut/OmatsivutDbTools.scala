package fi.vm.sade.omatsivut

import java.util.UUID
import java.util.concurrent.TimeUnit

import fi.vm.sade.omatsivut.db.impl.OmatsivutDb
import fi.vm.sade.omatsivut.security.{Hetu, OppijaNumero, SessionId, SessionInfo}
import org.specs2.mutable.Specification
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.duration.Duration


trait OmatsivutDbTools extends Specification {

  val singleConnectionOmatsivutDb: OmatsivutDb

  def createTestSession()(implicit personOid: PersonOid): String = {
    val dummyHetu = Hetu("121212-789A")
    val dummyName = "John Smith"
    singleConnectionOmatsivutDb.store(SessionInfo(dummyHetu, OppijaNumero(personOid.oid), dummyName)).value.toString
  }

  def setSessionLastAccessTime(sessionIdString: String, howManySecondsFromNow: Int) = {
    singleConnectionOmatsivutDb.runBlocking(DBIO.seq(
      sqlu"""update sessions
                        set viimeksi_luettu = now() - interval '#${howManySecondsFromNow} seconds'
                        where id = ${sessionIdString}::uuid"""
    ).transactionally, Duration(20, TimeUnit.SECONDS))
  }

  def getPersonFromSession(sessionIdString: String): Option[String] = {
    val sessionId = SessionId(UUID.fromString(sessionIdString))
    singleConnectionOmatsivutDb.get(sessionId) match {
      case Some(SessionInfo(hetu, oppijaNumero, oppijaNimi)) => Some(oppijaNumero.value)
      case _ => None
    }
  }

  def getDisplayNameFromSession(sessionIdString: String): Option[String] = {
    val sessionId = SessionId(UUID.fromString(sessionIdString))
    singleConnectionOmatsivutDb.get(sessionId) match {
      case Some(SessionInfo(hetu, oppijaNumero, oppijaNimi)) => Some(oppijaNimi)
      case _ => None
    }
  }

  def deleteAllSessions(): Unit = {
    singleConnectionOmatsivutDb.runBlocking(DBIO.seq(
      sqlu"truncate table sessions cascade"
      ).transactionally)
  }
}
