package fi.vm.sade.omatsivut.servlet.session

import javax.servlet.http.HttpServletRequest

import fi.vm.sade.hakemuseditori.auditlog.{AuditLogger, AuditLoggerComponent}
import fi.vm.sade.omatsivut.auditlog.Logout
import fi.vm.sade.omatsivut.security.AuthenticationContext
import fi.vm.sade.omatsivut.security.AuthenticationInfoParser._
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import org.scalatra.servlet.RichResponse

trait LogoutServletContainer {
  this: AuditLoggerComponent =>

  val auditLogger: AuditLogger

  class LogoutServlet(val authenticationContext: AuthenticationContext) extends OmatSivutServletBase {
    get("/*") {
      redirectToShibbolethLogout(request, response)
    }

    def sendLogOut {
      auditLogger.log(Logout(getAuthenticationInfo(request)))
      redirectToShibbolethLogout(request, response)
    }

    def redirectToShibbolethLogout(request: HttpServletRequest, response: RichResponse): Unit = {
      val returnUrl = request.getContextPath + "/session/reset"
      response.redirect(authenticationContext.ssoContextPath + "/Shibboleth.sso/Logout?return=" + returnUrl)
    }
  }
}

