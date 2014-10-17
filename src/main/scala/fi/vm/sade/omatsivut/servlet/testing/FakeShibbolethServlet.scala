package fi.vm.sade.omatsivut.servlet.testing

import javax.servlet.http.{Cookie, HttpServletResponse, HttpServletRequest}

import fi.vm.sade.omatsivut.config.AppConfig
import AppConfig.AppConfig
import fi.vm.sade.omatsivut.security.AuthCookieParsing
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase

class FakeShibbolethServlet(val appConfig: AppConfig) extends OmatSivutServletBase with AuthCookieParsing  {
  if(appConfig.usesFakeAuthentication){
    get("/Logout") {
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

  private def tellBrowserToDeleteShibbolethCookie(req: HttpServletRequest, res: HttpServletResponse) {
    tellBrowserToDeleteCookie(res, reqCookie(req, {_.getName.startsWith("_shibsession_")}))
  }

  private def tellBrowserToDeleteCookie(res: HttpServletResponse, cookie: Option[Cookie]) = {
    cookie.map(c => {
      c.setPath("/")
      c.setMaxAge(0)
      res.addCookie(c)
    })
  }

  def redirectToFakeLogin {
    response.redirect(request.getContextPath + "/fakeVetumaLogin.html")
  }
}
