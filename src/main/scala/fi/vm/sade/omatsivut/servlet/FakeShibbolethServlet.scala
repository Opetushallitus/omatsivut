package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.security.AuthCookieParsing

class FakeShibbolethServlet(implicit val appConfig: AppConfig) extends OmatSivutServletBase with AuthCookieParsing  {
  if(appConfig.isTest){
    get("/Logout") {
      tellBrowserToDeleteAuthCookie(request, response)
      response.redirect(request.getContextPath + "/index.html")
    }

    get("/LoginFI") {
      response.redirect(request.getContextPath + "/fakeVetumaLogin.html")
    }
  }
}
