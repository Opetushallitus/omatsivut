package fi.vm.sade.omatsivut.servlet

import java.util.UUID
import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.omatsivut.security.{AttributeNames, SessionId, SessionService}
import fi.vm.sade.omatsivut.servlet.session.OmatsivutPaths
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra.{BadRequest, ScalatraFilter}

import java.net.URL

class AuthenticateIfNoSessionFilter(val sessionService: SessionService)
  extends ScalatraFilter with OmatsivutPaths with AttributeNames with Logging {

  implicit def language: Language.Language = {
    Option(request.getAttribute("lang").asInstanceOf[Language.Language]).getOrElse(Language.fi)
  }

  implicit def domain: String = {
    val url: URL = new URL(request.getRequestURL.toString)
    url.getHost
  }

  before() {
    val sessionCookie: Option[String] = cookies.get(sessionCookieName)
    val sessionUUID: Option[UUID] = try {
      sessionCookie.map(UUID.fromString)
    } catch {
      case e: Throwable =>
        logger.warn(s"Failed to verify session with id=$sessionCookie", e)
        halt(BadRequest(s"Problem verifying the session with id=$sessionCookie ($e)"))
    }
    val sessionId: Option[SessionId] = sessionUUID.map(SessionId)
    sessionService.getSession(sessionId) match {
      case Right(sessionInfo) =>
        logger.debug("Found session: " + sessionInfo.oppijaNumero)
        pass()
      case _ =>
        logger.debug("Session not found, redirect to login")
        val path = request.getContextPath
        val url = loginPath(path)
        redirect(url)
    }
  }
}
