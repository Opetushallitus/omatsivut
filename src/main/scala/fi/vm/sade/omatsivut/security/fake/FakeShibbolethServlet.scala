package fi.vm.sade.omatsivut.security.fake

import javax.servlet.http.{HttpServletRequest, HttpServletResponse, Cookie => HttpCookie}

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.security.{AuthenticationCipher, AuthenticationInfoParsing, ShibbolethCookie}
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import org.scalatra.{Cookie, CookieOptions}

/**
 * Simulates the actual Shibboleth server with AA (Attribute Authority) that provides the SSN->personOid mapping.
 *
 * Provides /fakesession?hetu=123456-7890 url for starting a session with given SSN.
 *
 * @param appConfig
 */
class FakeShibbolethServlet(val appConfig: AppConfig) extends OmatSivutServletBase with AuthenticationInfoParsing  {
  get("/fakesession") {
    val shibbolethCookie = ShibbolethCookie("_shibsession_fakeshibbolethsession", new AuthenticationCipher(appConfig.settings.aesKey, appConfig.settings.hmacKey).encrypt("FAKESESSION"))
    response.addCookie(fakeShibbolethSessionCookie(shibbolethCookie))
    paramOption("hetu") match {
      case Some(hetu) =>
        response.addCookie(Cookie(FakeAuthentication.oidCookie, TestFixture.persons.get(hetu).getOrElse(""))(appConfig.authContext.cookieOptions))
        response.redirect(request.getContextPath + "/secure/initsession?hetu=" + hetu)
      case _ => halt(400, "Can't fake session without ssn")
    }
  }

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

  private def tellBrowserToDeleteShibbolethCookie(req: HttpServletRequest, res: HttpServletResponse) {
    tellBrowserToDeleteCookie(res, reqCookie(req, {_.getName.startsWith("_shibsession_")}))
  }

  private def tellBrowserToDeleteCookie(res: HttpServletResponse, cookie: Option[HttpCookie]) = {
    cookie.map(c => {
      c.setPath("/")
      c.setMaxAge(0)
      res.addCookie(c)
    })
  }

  private def redirectToFakeLogin {
    response.redirect(request.getContextPath + "/fakeVetumaLogin.html")
  }

  private def fakeShibbolethSessionCookie(shibbolethSessionData: ShibbolethCookie): Cookie = {
    Cookie(shibbolethSessionData.name, shibbolethSessionData.value)(CookieOptions(path = "/"))
  }
}
