package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.auditlog.AuditLogger
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import fi.vm.sade.omatsivut.util.Logging
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.scalatra.{NotFound, Unauthorized}

trait AuthenticationRequiringServlet extends OmatSivutServletBase with AuthenticationInfoParsing with Logging {
  val appConfig: AppConfig

  def personOid() = personOidOption(request).getOrElse(sys.error("Unauthenticated account"))

  before() {
    shibbolethCookieInRequest(request) match {
      case Some(cookie) => personOidOption(request) match {
        case Some(oid) if !oid.isEmpty =>
          true
        case _ =>
          halt(NotFound(render("error" -> "no oid was present")))
      }
      case _ =>
        halt(Unauthorized(render("error" -> "unauthorized")))
    }
  }
}
