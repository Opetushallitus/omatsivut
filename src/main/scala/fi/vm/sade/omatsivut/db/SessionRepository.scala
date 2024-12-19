package fi.vm.sade.omatsivut.db

import fi.vm.sade.omatsivut.SessionFailure.SessionFailure
import fi.vm.sade.omatsivut.security.{SessionId, SessionInfo}

trait SessionRepository {
  def sessionTimeoutSeconds: Int = 3600
  def store(session: SessionInfo): SessionId
  def get(id: SessionId): Either[SessionFailure, SessionInfo]
  def delete(id: SessionId): Unit
  def deleteByServiceTicket(ticket: String): Unit
  def deleteExpired(): Int
}
