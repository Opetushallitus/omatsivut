package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.security.AuthenticationInfoParser._
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import fi.vm.sade.omatsivut.util.Logging
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.scalatra.{InternalServerError, NotFound, Unauthorized}

trait AuthenticationRequiringServlet extends OmatSivutServletBase with Logging {
  val appConfig: AppConfig

  def personOid() = getAuthenticationInfo(request).personOid.getOrElse(sys.error("Unauthenticated account"))

  before() {
    getAuthenticationInfo(request) match {
      case AuthenticationInfo(_, _, Some(error)) =>
        halt(InternalServerError("error" -> error))
      case AuthenticationInfo(_, None, _) =>
        halt(Unauthorized(render("error" -> "unauthorized")))
      case AuthenticationInfo(None, _, _) =>
        halt(NotFound(render("error" -> "no oid was present")))
      case AuthenticationInfo(Some(oid), _, _) =>
        true
    }
  }
}
