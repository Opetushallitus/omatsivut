package fi.vm.sade.omatsivut.security

import java.util.UUID

import fi.vm.sade.omatsivut.security.SessionInfoRetriever._
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.scalatra.{NotFound, Ok, Unauthorized}

trait AuthenticationRequiringServlet extends OmatSivutServletBase with Logging {
  implicit def sessionService: SessionService

  def personOid(): String = getOppijaNumero(request).getOrElse(sys.error("Unauthenticated account"))

  before() {
    sessionService.getSession(
      cookies.get(sessionCookieName).map(UUID.fromString).map(SessionId)
    ) match {
      case Right(sessionInfo) =>
        logger.debug("Found session: " + sessionInfo.oppijaNumero)
        session.setAttribute(sessionInfoAttributeName, sessionInfo)
        if (sessionInfo.oppijaNumero.value.isEmpty) {
          logger.debug("Session has no oppijaNumero, should not find anything")
          halt(NotFound(render("error" -> "no oid was present")))
        }
      case _ =>
        logger.debug("Session not found, fail the API request")
        halt(Unauthorized(render("error" -> "unauthorized")))
    }
  }
}
