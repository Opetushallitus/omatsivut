package fi.vm.sade.omatsivut.servlet.testing

import fi.vm.sade.omatsivut.config.AppConfig
import AppConfig.AppConfig
import fi.vm.sade.omatsivut.security.AuthCookieParsing
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase

class FakeShibbolethServlet(implicit val appConfig: AppConfig) extends OmatSivutServletBase with AuthCookieParsing  {
  if(appConfig.usesFakeAuthentication){
    get("/Logout") {
      tellBrowserToDeleteAuthCookie(request, response)
      tellBrowserToDeleteShibbolethCookie(request, response)
      paramOption("return") match {
        case Some(url) => response.redirect(url)
        case _ => redirectToFakeLogin
      }
    }

    get("/Login*") {
      redirectToFakeLogin
    }
  }

  def redirectToFakeLogin {
    response.redirect(request.getContextPath + "/fakeVetumaLogin.html")
  }
}
