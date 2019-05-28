package fi.vm.sade.omatsivut.servlet

import java.net.URLEncoder
import java.util.UUID

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.omatsivut.security.SessionInfoRetriever.sessionCookieName
import fi.vm.sade.omatsivut.security.{AttributeNames, SessionId, SessionService}
import fi.vm.sade.omatsivut.servlet.session.OmatsivutPaths
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra.ScalatraFilter

class AuthenticateIfNoSessionFilter(val sessionService: SessionService)
  extends ScalatraFilter with OmatsivutPaths with AttributeNames with Logging {

  implicit def language: Language.Language = {
    Option(request.getAttribute("lang").asInstanceOf[Language.Language]).getOrElse(Language.fi)
  }

  before() {
    val sessionCookie: Option[String] = cookies.get(sessionCookieName)
    val sessionUUID: Option[UUID] = sessionCookie.map(UUID.fromString)
    val sessionId: Option[SessionId] = sessionUUID.map(SessionId)
    sessionService.getSession(sessionId) match {
      case Right(sessionInfo) =>
        logger.debug("Found session: " + sessionInfo.oppijaNumero)
        session.setAttribute(sessionInfoAttributeName, sessionInfo)
      case _ =>
        logger.debug("Session not found, redirect to login")
        response.redirect(loginPath(request.getContextPath))
    }
  }

  after() {
    // clean the http session, to avoid sessioninfo hanging in session object and maybe misleading somebody
    session.removeAttribute(sessionInfoAttributeName)
  }

}
