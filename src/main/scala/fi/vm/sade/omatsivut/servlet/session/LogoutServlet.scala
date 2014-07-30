package fi.vm.sade.omatsivut.servlet.session

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.auditlog.{AuditLogger, Logout}
import fi.vm.sade.omatsivut.security.{AuthCookieParsing, CookieCredentials}
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase

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
