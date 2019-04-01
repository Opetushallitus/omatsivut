package fi.vm.sade.omatsivut.db

import fi.vm.sade.omatsivut.security.{SessionId, SessionInfo}

trait SessionRepository {
  def store(session: SessionInfo): SessionId
  def get(id: SessionId): Option[SessionInfo]
  def delete(id: SessionId): Unit
}
