package fi.vm.sade.omatsivut.security

import javax.servlet.http.{Cookie, HttpServletRequest}
import fi.vm.sade.utils.slf4j.Logging

object SessionInfoRetriever extends Logging with AttributeNames {
  def getSessionId(request: HttpServletRequest): Option[String] = {
    val sessionIdCookie: Option[Cookie] = CookieHelper.getCookie(request, sessionCookieName)
    sessionIdCookie.map(_.getValue)
  }

  def getOppijaNumero(request: HttpServletRequest): Option[String] = {
    val sessionInfo = getSessionInfo(request)
    sessionInfo.map(_.oppijaNumero.value)
  }

  def getOppijaNimi(request: HttpServletRequest): Option[String] = {
    val sessionInfo = getSessionInfo(request)
    sessionInfo.map(_.oppijaNimi)
  }

  def getSessionInfo(request: HttpServletRequest): Option[SessionInfo] = {
    val session = request.getSession
    val sessionInfoAttribute = session.getAttribute(sessionInfoAttributeName).asInstanceOf[SessionInfo]
    if (sessionInfoAttribute != null) Some(sessionInfoAttribute) else None
  }
}
