package fi.vm.sade.omatsivut.servlet.session

import fi.vm.sade.omatsivut.config.AppConfig
import AppConfig.AppConfig
import fi.vm.sade.omatsivut.auditlog.{AuditLoggerComponent, AuditLogger, Logout}
import fi.vm.sade.omatsivut.security.{AuthenticationCipher, AuthCookieParsing, CookieCredentials}
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase

trait LogoutServletContainer {
  this: AuditLoggerComponent =>

  val auditLogger: AuditLogger

  class LogoutServlet(implicit val appConfig: AppConfig) extends OmatSivutServletBase with AuthCookieParsing with ShibbolethPaths {
    get("/*") {
      parseCredentials(request, new AuthenticationCipher(appConfig)) match {
        case Some(credentials) => sendLogOut(credentials)
        case _ => redirectToShibbolethLogout(request, response)
      }
    }

    def sendLogOut(credentials: CookieCredentials) {
      auditLogger.log(Logout(credentials))
      tellBrowserToDeleteAuthCookie(request, response)
      redirectToShibbolethLogout(request, response)
    }
  }
}

