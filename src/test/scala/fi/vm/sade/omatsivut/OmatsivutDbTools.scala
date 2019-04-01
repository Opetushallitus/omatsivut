package fi.vm.sade.omatsivut

import java.util.UUID

import fi.vm.sade.omatsivut.db.impl.OmatsivutDb
import fi.vm.sade.omatsivut.security.{Hetu, OppijaNumero, SessionInfo, SessionId}
import org.specs2.mutable.Specification
import slick.jdbc.PostgresProfile.api._


trait OmatsivutDbTools extends Specification {

  val singleConnectionOmatsivutDb: OmatsivutDb

  def createTestSession()(implicit personOid: PersonOid) = {
    val dummyHetu = Hetu("121212-789A")
    val dummyName = "John Smith"
    singleConnectionOmatsivutDb.store(SessionInfo(dummyHetu, OppijaNumero(personOid.oid), dummyName)).value.toString
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
