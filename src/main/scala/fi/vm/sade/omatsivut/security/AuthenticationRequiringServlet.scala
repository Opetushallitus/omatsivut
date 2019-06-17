package fi.vm.sade.omatsivut.security

import java.util.UUID

import fi.vm.sade.omatsivut.security.SessionInfoRetriever._
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.scalatra.{BadRequest, NotFound, Ok, Unauthorized}

trait AuthenticationRequiringServlet extends OmatSivutServletBase with Logging {
  implicit def sessionService: SessionService
  val returnNotFoundIfNoOid = true

  def personOid(): String = getOppijaNumero(request).getOrElse(sys.error("Unauthenticated account"))

  before() {
    val sessionCookie: Option[String] = cookies.get(sessionCookieName)
    val sessionUUID: Option[UUID] = try {
      sessionCookie.map(UUID.fromString)
    } catch {
      case e: Throwable =>
        halt(BadRequest(s"Problem verifying the session with id=$sessionCookie ($e)"))
    }
    val sessionId: Option[SessionId] = sessionUUID.map(SessionId)
    sessionService.getSession(sessionId) match {
      case Right(sessionInfo) =>
        logger.debug("Found session: " + sessionInfo.oppijaNumero)
        if (returnNotFoundIfNoOid && sessionInfo.oppijaNumero.value.isEmpty) {
          logger.info("Session has no oppijaNumero, should not find anything")
          halt(NotFound(render("error" -> "no oid was present")))
        }
        pass()
      case _ =>
        logger.info("Session not found, fail the API request")
        halt(Unauthorized(render("error" -> "unauthorized")))
    }
  }
}
