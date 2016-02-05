package fi.vm.sade.omatsivut.security.fake

import javax.servlet.http.{HttpServletRequest, HttpServletResponse, Cookie => HttpCookie}

import fi.vm.sade.omatsivut.config.AppConfig.{AppConfig, StubbedExternalDeps}
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.security.CookieHelper.reqCookie
import fi.vm.sade.omatsivut.security.{AuthenticationCipher, RemoteAuthenticationInfoService, ShibbolethCookie}
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import org.http4s.ParseException
import org.scalatra.{Cookie, CookieOptions}

/**
 * Simulates the actual Shibboleth server with AA (Attribute Authority) that provides the SSN->personOid mapping.
 *
 * Provides /fakesession?hetu=123456-7890 url for starting a session with given SSN.
 *
 * @param appConfig
 */
class FakeShibbolethServlet(val appConfig: AppConfig) extends OmatSivutServletBase {
  get("/fakesession") {
    val shibbolethCookie = ShibbolethCookie("_shibsession_fakeshibbolethsession", new AuthenticationCipher(appConfig.settings.aesKey, appConfig.settings.hmacKey).encrypt("FAKESESSION"))
    response.addCookie(fakeShibbolethSessionCookie(shibbolethCookie))
    paramOption("hetu") match {
      case Some(hetu) =>
        val personOid: Option[String] = getPersonOidByHetu(hetu)
        response.addCookie(Cookie(FakeAuthentication.oidCookie, personOid.getOrElse(""))(appConfig.authContext.cookieOptions))
        response.redirect(request.getContextPath + "/secure/initsession?hetu=" + hetu)
      case _ => halt(400, "Can't fake session without ssn")
    }
  }

  def getPersonOidByHetu(hetu: String): Option[String] = {
    appConfig match {
      case c: StubbedExternalDeps => {
        TestFixture.persons.get(hetu)
      }
      case _ => {
        val service = new RemoteAuthenticationInfoService(appConfig.settings.authenticationServiceConfig, appConfig.settings.securitySettings)
        try {
          service.getHenkiloOID(hetu)
        }
        catch {
          case e: ParseException => {
            logger.error(e.failure.details)
            throw e
          }
        }
      }
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
