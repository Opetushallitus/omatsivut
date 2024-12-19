package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.SessionFailure
import fi.vm.sade.omatsivut.db.SessionRepository
import fi.vm.sade.omatsivut.util.Logging

import scala.util.{Failure, Success, Try}


class SessionService(val sessionRepository: SessionRepository) extends Logging {

  def deleteSession(sessionId: Option[SessionId]): Unit = sessionId match {
    case None => logger.debug("no sessionId given")
    case Some(id) => {
      Try(sessionRepository.delete(id)) match {
        case Success(_) => logger.debug("Session " + id + " removed from database")
        case Failure(t) => logger.error("Failed to remove session " + id + " from database", t)
      }
    }
  }

  def deleteSessionByServiceTicket(ticket: String): Unit = {
      Try(sessionRepository.deleteByServiceTicket(ticket)) match {
        case Success(_) => logger.debug("Session " + ticket + " removed from database")
        case Failure(t) => logger.error("Failed to remove session " + ticket + " from database", t)
      }

  }

  def deleteAllExpired(): Unit = {
    Try(sessionRepository.deleteExpired()) match {
      case Success(count) => logger.info("Deleted " + count + " expired sessions from database")
      case Failure(t) => logger.error("Failed to delete expired sessions from database", t)
    }
  }

  def storeSession(ticket: String, hetu: Hetu, oppijaNumero: OppijaNumero, oppijaNimi: String): Either[Throwable, (SessionId, SessionInfo)] = {
    val session = SessionInfo(ticket, hetu, oppijaNumero, oppijaNimi)
    logger.debug("Storing to session: " + session.oppijaNumero)
    Try(sessionRepository.store(session)) match {
      case Success(id) => Right((id, session))
      case Failure(t) => Left(t)
    }
  }

  def getSession(id: Option[SessionId]): Either[Throwable, SessionInfo] = id match {
    case None => Left(new AuthenticationFailedException(s"No credentials given"))
    case Some(id) => {
      Try(sessionRepository.get(id)) match {
        case Success(Right(session)) => Right(session)
        case Success(Left(SessionFailure.SESSION_NOT_FOUND)) => {
          logger.info(s"Session $id does not exist")
          Left(new AuthenticationFailedException(s"Session $id doesn't exist"))
        }
        case Success(Left(SessionFailure.SESSION_EXPIRED)) => Left(new AuthenticationFailedException(s"Session $id expired"))
        case Failure(t) => Left(t)
        case e => Left(new RuntimeException("Unexpected result from getSession"))
      }
    }
  }

}
