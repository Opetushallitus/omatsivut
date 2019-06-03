package fi.vm.sade.omatsivut.security

import java.util.UUID

import javax.servlet.http.{Cookie, HttpServletRequest}
import fi.vm.sade.utils.slf4j.Logging

object SessionInfoRetriever extends Logging with AttributeNames {
  def getSessionId(request: HttpServletRequest): Option[String] = {
    val sessionIdCookie: Option[Cookie] = CookieHelper.getCookie(request, sessionCookieName)
    sessionIdCookie.map(_.getValue)
  }

  def getOppijaNumero(request: HttpServletRequest)(implicit sessionService: SessionService): Option[String] = {
    val sessionInfo = getSessionInfo(request)
    sessionInfo.map(_.oppijaNumero.value)
  }

  def getOppijaNimi(request: HttpServletRequest)(implicit sessionService: SessionService): Option[String] = {
    val sessionInfo = getSessionInfo(request)
    sessionInfo.map(_.oppijaNimi)
  }

  def getSessionInfo(request: HttpServletRequest)(implicit sessionService: SessionService): Option[SessionInfo] = {
    val sessionCookie: Option[String] = getSessionId(request)
    sessionCookie.flatMap(sessionIdFromCookie => {
      val sessionUUID: Option[UUID] = try {
        Some(UUID.fromString(sessionIdFromCookie))
      } catch {
        case e: Throwable =>
          logger.error(s"Problem verifying the session with id=$sessionIdFromCookie ($e)")
          None
      }
      val sessionId: Option[SessionId] = sessionUUID.map(SessionId)
      sessionService.getSession(sessionId) match {
        case Right(sessionInfo) => Some(sessionInfo)
        case Left(t) =>
          logger.error(s"Error reading session id=$sessionIdFromCookie ($t)")
          None
      }
    })
  }
}
