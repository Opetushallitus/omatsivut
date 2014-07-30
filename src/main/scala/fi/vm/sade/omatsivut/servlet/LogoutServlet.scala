package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.auditlog.{Logout, AuditLogger}
import fi.vm.sade.omatsivut.security.{CookieCredentials, AuthCookieParsing}

class LogoutServlet(implicit val appConfig: AppConfig) extends OmatSivutServletBase with AuthCookieParsing with ShibbolethPaths {
  get("/*") {
    parseCredentials(request) match {
      case Some(credentials) => sendLogOut(credentials)
      case _ => redirectToShibbolethLogout(request, response)
    }
  }

  def sendLogOut(credentials: CookieCredentials) {
    AuditLogger.log(Logout(credentials))
    tellBrowserToDeleteAuthCookie(request, response)
    redirectToShibbolethLogout(request, response)
  }
}
