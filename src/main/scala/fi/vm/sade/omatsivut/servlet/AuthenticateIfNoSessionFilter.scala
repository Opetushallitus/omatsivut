package fi.vm.sade.omatsivut.servlet

import java.net.URLEncoder
import java.util.UUID

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.omatsivut.security.{CookieNames, SessionId, SessionService}
import fi.vm.sade.omatsivut.servlet.session.OmatsivutPaths
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra.ScalatraFilter

class AuthenticateIfNoSessionFilter(val sessionService: SessionService)
  extends ScalatraFilter with OmatsivutPaths with CookieNames with Logging {

  implicit def language: Language.Language = {
    Option(request.getAttribute("lang").asInstanceOf[Language.Language]).getOrElse(Language.fi)
  }

  before() {
    sessionService.getSession(
      cookies.get(sessionCookieName).map(UUID.fromString).map(SessionId)
    ) match {
      case Right(session) =>
        logger.debug("Found session: " + session.oppijaNumero)
      case _ =>
        logger.debug("Session not found, redirect to login")
        response.redirect(shibbolethPath(request.getContextPath))
    }
  }

}
