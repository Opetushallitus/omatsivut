package fi.vm.sade.omatsivut.db

import fi.vm.sade.omatsivut.security.{SessionId, Session}

trait SessionRepository {
  def store(session: Session): SessionId
  def get(id: SessionId): Option[Session]
  def delete(id: SessionId): Unit
}
