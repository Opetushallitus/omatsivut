package fi.vm.sade.omatsivut

import java.util.UUID

import fi.vm.sade.omatsivut.db.impl.OmatsivutDb
import fi.vm.sade.omatsivut.security.{SessionId, OppijaNumero, Session}
import org.specs2.mutable.Specification
import slick.jdbc.PostgresProfile.api._


trait OmatsivutDbTools extends Specification {

  val singleConnectionOmatsivutDb: OmatsivutDb

  def createTestSession()(implicit personOid: PersonOid) = {
    singleConnectionOmatsivutDb.store(Session(OppijaNumero(personOid.oid))).value.toString
  }

  def getPersonFromSession(sessionIdString: String): Option[String] = {
    val sessionId = SessionId(UUID.fromString(sessionIdString))
    singleConnectionOmatsivutDb.get(sessionId) match {
      case Some(Session(oppijaNumero)) => Some(oppijaNumero.value)
      case _ => None
    }
  }

  def deleteAllSessions(): Unit = {
    singleConnectionOmatsivutDb.runBlocking(DBIO.seq(
      sqlu"truncate table sessions cascade"
      ).transactionally)
  }
}
