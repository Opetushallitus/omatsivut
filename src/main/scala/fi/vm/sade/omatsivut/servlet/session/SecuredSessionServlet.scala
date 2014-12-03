package fi.vm.sade.omatsivut.servlet.session

import javax.servlet.http.HttpServletRequest

import fi.vm.sade.omatsivut.auditlog.{AuditLogger, Login}
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.security._
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase

trait SecuredSessionServletContainer {
  val auditLogger: AuditLogger

  class SecuredSessionServlet(val appConfig: AppConfig) extends OmatSivutServletBase with AuthenticationInfoParsing with ShibbolethPaths {
    get("/initsession") {
      personOidOption(request.asInstanceOf[HttpServletRequest]) match {
        case (Some(oid)) => {
          auditLogger.log(Login(authInfo(request)))
          response.redirect(redirectUri)
        }
        case _ => redirectToShibbolethLogin(response, appConfig.authContext.ssoContextPath)
      }
    }

    private def redirectUri: String = {
      request.getContextPath + paramOption("redirect").getOrElse("/index.html")
    }
  }
}