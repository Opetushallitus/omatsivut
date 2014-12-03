package fi.vm.sade.omatsivut.servlet.session

import fi.vm.sade.omatsivut.auditlog.{AuditLogger, Login}
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.security.AuthenticationInfoParser._
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase

trait SecuredSessionServletContainer {
  val auditLogger: AuditLogger

  class SecuredSessionServlet(val appConfig: AppConfig) extends OmatSivutServletBase with ShibbolethPaths {
    get("/initsession") {
      val info = getAuthenticationInfo(request)
      info.personOid match {
        case (Some(oid)) => {
          auditLogger.log(Login(info))
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