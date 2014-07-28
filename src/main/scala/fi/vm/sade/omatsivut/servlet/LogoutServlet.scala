package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.security.AuthCookieParsing

class LogoutServlet(implicit val appConfig: AppConfig) extends OmatSivutServletBase with AuthCookieParsing {
  get("/logout") {
    tellBrowserToDeleteAuthCookie(request, response)
    response.redirect(appConfig.authContext.ssoContextPath + "/Shibboleth.sso/Logout?type=Local")
  }
}
