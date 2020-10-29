package fi.vm.sade.omatsivut.db

import fi.vm.sade.omatsivut.SessionFailure.SessionFailure
import fi.vm.sade.omatsivut.security.{SessionId, SessionInfo}
import fi.vm.sade.utils.cas.CasClient.ServiceTicket

trait SessionRepository {
  def sessionTimeoutSeconds: Int = 3600
  def store(session: SessionInfo): SessionId
  def get(id: SessionId): Either[SessionFailure, SessionInfo]
  def delete(id: SessionId): Unit
  def deleteByServiceTicket(ticket: ServiceTicket): Unit
  def deleteExpired(): Int
}
