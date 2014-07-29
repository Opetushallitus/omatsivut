package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.auditlog.AuditLogger
import fi.vm.sade.omatsivut.security.{CookieCredentials, AuthCookieParsing}

class LogoutServlet(implicit val appConfig: AppConfig) extends OmatSivutServletBase with AuthCookieParsing {
  get("/logout") {
    parseCredentials(request) match {
      case Some(credentials) => sendLogOut(credentials)
      case _ => redirectToIndex
    }
  }

  def sendLogOut(credentials: CookieCredentials) {
    AuditLogger.logLogout(credentials)
    tellBrowserToDeleteAuthCookie(request, response)
    val returnUrl = request.getContextPath + "/session/reset"
    response.redirect(appConfig.authContext.ssoContextPath + "/Shibboleth.sso/Logout?return=" + returnUrl)
  }

  get("/reset") {
    redirectToIndex
  }

  def redirectToIndex {
    response.redirect(request.getContextPath + "/index.html")
  }
}
