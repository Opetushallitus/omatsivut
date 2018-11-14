package fi.vm.sade.omatsivut.security

import java.util.UUID

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.security.AuthenticationInfoParser._
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.scalatra.{NotFound, Unauthorized}

trait AuthenticationRequiringServlet extends OmatSivutServletBase with Logging {
  val appConfig: AppConfig

  implicit def sessionService: SessionService

  def personOid(): String = getAuthenticationInfo(request).oppijaNumero.getOrElse(sys.error("Unauthenticated account"))

  before() {
    val AuthenticationInfo(personOidOption, sessionIdOption) = getAuthenticationInfo(request)
    sessionService.getSession(
      sessionIdOption.map(UUID.fromString).map(SessionId)
    ) match {
      case Right(session) =>
        logger.debug("Found session: " + session.oppijaNumero)
      case _ =>
        halt(Unauthorized(render("error" -> "unauthorized")))
    }
  }
}
