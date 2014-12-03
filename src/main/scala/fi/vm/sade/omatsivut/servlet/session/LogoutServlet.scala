package fi.vm.sade.omatsivut.servlet.session

import javax.servlet.http.HttpServletRequest

import fi.vm.sade.omatsivut.auditlog.{AuditLogger, AuditLoggerComponent, Logout}
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.security.AuthenticationInfoParsing
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import org.scalatra.servlet.RichResponse

trait LogoutServletContainer {
  this: AuditLoggerComponent =>

  val auditLogger: AuditLogger

  class LogoutServlet(val appConfig: AppConfig) extends OmatSivutServletBase with AuthenticationInfoParsing {
    get("/*") {
      redirectToShibbolethLogout(request, response)
    }

    def sendLogOut {
      auditLogger.log(Logout(authInfo(request)))
      redirectToShibbolethLogout(request, response)
    }

    def redirectToShibbolethLogout(request: HttpServletRequest, response: RichResponse): Unit = {
      val returnUrl = request.getContextPath + "/session/reset"
      response.redirect(appConfig.authContext.ssoContextPath + "/Shibboleth.sso/Logout?return=" + returnUrl)
    }
  }
}

