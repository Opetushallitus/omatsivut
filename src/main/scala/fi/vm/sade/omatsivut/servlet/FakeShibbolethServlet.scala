package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.security.AuthCookieParsing

class FakeShibbolethServlet(implicit val appConfig: AppConfig) extends OmatSivutServletBase with AuthCookieParsing  {
  if(appConfig.usesFakeAuthentication){
    get("/Logout") {
      tellBrowserToDeleteAuthCookie(request, response)
      paramOption("return") match {
        case Some(url) => response.redirect(url)
        case _ => redirectToFakeLogin
      }
    }

    get("/LoginFI") {
      redirectToFakeLogin
    }
  }

  def redirectToFakeLogin {
    response.redirect(request.getContextPath + "/fakeVetumaLogin.html")
  }
}
