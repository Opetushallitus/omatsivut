package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.SessionFailure
import fi.vm.sade.omatsivut.db.SessionRepository
import fi.vm.sade.utils.slf4j.Logging

import scala.util.{Failure, Success, Try}


class SessionService(val sessionRepository: SessionRepository) extends Logging {

  def deleteSession(sessionId: Option[SessionId]): Unit = sessionId match {
    case None => logger.debug("no sessionId given")
    case Some(id) => {
      Try(sessionRepository.delete(id)) match {
        case Success(_) => logger.debug("session " + id + " removed from database")
        case Failure(t) => logger.info("Did not manage to remove session " + id + " from database", t)
      }
    }
  }

  def storeSession(hetu: Hetu, oppijaNumero: OppijaNumero, oppijaNimi: String): Either[Throwable, (SessionId, SessionInfo)] = {
    val session = SessionInfo(hetu, oppijaNumero, oppijaNimi)
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
