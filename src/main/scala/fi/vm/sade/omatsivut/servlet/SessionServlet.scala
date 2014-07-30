package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.auditlog.{AuditLogger, Logout}
import fi.vm.sade.omatsivut.security.{AuthCookieParsing, CookieCredentials}

class SessionServlet(implicit val appConfig: AppConfig) extends OmatSivutServletBase with AuthCookieParsing with ShibbolethPaths {
  get("/logout") {
    parseCredentials(request) match {
      case Some(credentials) => sendLogOut(credentials)
      case _ => redirectToShibbolethLogout(request, response)
    }
  }

  get("/login") {
    redirectToShibbolethLogin(response)
  }

  def sendLogOut(credentials: CookieCredentials) {
    AuditLogger.log(Logout(credentials))
    tellBrowserToDeleteAuthCookie(request, response)
    redirectToShibbolethLogout(request, response)
  }

  get("/session/reset") {
    redirectToIndex
  }

  def redirectToIndex {
    // TODO: Redirect to domain root when login links in place, e.g. :
    // val redirectUrl = if (appConfig.usesFakeAuthentication) request.getContextPath + "/index.html" else "/"
    // response.redirect(redirectUrl)
    response.redirect(request.getContextPath + "/index.html")
  }
}
