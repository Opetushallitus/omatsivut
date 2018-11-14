package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.db.SessionRepository
import fi.vm.sade.utils.slf4j.Logging

import scala.util.{Failure, Success, Try}


class SessionService(sessionRepository: SessionRepository) extends Logging {

  def deleteSession(sessionId: Option[SessionId]): Unit = sessionId match {
    case None => logger.debug("no sessionId given")
    case Some(id) => {
      Try(sessionRepository.delete(id)) match {
        case Success(_) => logger.debug("session " + id + " removed from database")
        case Failure(t) => logger.debug("Did not manage to remove session " + id + " from database, because of " + t)
      }
    }
  }


  def storeSession(oppijaNumero: OppijaNumero): Either[Throwable, (SessionId, Session)] = {
    val session = Session(oppijaNumero)
    logger.debug("Storing to session: " + session.oppijaNumero)
    Try(sessionRepository.store(session)) match {
      case Success(id) => Right((id, session))
      case Failure(t) => Left(t)
    }
  }

  def getSession(id: Option[SessionId]): Either[Throwable, Session] = id match {
    case None => Left(new AuthenticationFailedException(s"No credentials given"))
    case Some(id) => {
      Try(sessionRepository.get(id)) match {
        case Success(Some(session)) => Right(session)
        case Success(None) => Left(new AuthenticationFailedException(s"Session $id doesn't exist"))
        case Failure(t) => Left(t)
      }
    }
  }
}
