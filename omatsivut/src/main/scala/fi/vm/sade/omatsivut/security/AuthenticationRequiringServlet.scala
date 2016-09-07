package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.security.AuthenticationInfoParser._
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.scalatra.{NotFound, Unauthorized}

trait AuthenticationRequiringServlet extends OmatSivutServletBase with Logging {
  val appConfig: AppConfig

  def personOid(): String = getAuthenticationInfo(request).personOid.getOrElse(sys.error("Unauthenticated account"))

  before() {
    val AuthenticationInfo(personOidOption, shibbolethCookieOption) = getAuthenticationInfo(request)
    shibbolethCookieOption match {
      case Some(cookie) => personOidOption match {
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
