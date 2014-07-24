package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.fixtures.FixtureImporter
import fi.vm.sade.omatsivut.security.{ShibbolethCookie, AuthenticationCipher, AuthenticationInfoService}
import org.scalatra.{Cookie, CookieOptions}

class TestHelperServlet(implicit val appConfig: AppConfig) extends OmatSivutServletBase with AuthCookieCreating  {
  if(appConfig.isTest){
    get("/fakesession") {
      val hetuOption: Option[String] = paramOption("hetu")
      val shibbolethCookie = ShibbolethCookie("_shibsession_fakeshibbolethsession", AuthenticationCipher().encrypt("FAKESESSION"))
      createAuthCookieCredentials(hetuOption, Some(shibbolethCookie), AuthenticationInfoService.apply) match {
        case Some(credentials) => {
          response.addCookie(fakeShibbolethSessionCookie(shibbolethCookie))
          createAuthCookieResponse(credentials, response, redirectUri, CookieOptions(path = "/"))
        }
        case _ => response.redirect(ssoContextPath + "/Shibboleth.sso/LoginFI") //TODO Localization
      }
    }

    put("/fixtures/apply") {
      FixtureImporter().applyFixtures
    }
  }

  def fakeShibbolethSessionCookie(shibbolethSessionData: ShibbolethCookie): Cookie = {
    Cookie(shibbolethSessionData.name, shibbolethSessionData.value)(CookieOptions(path = "/"))
  }

  def redirectUri: String = {
    request.getContextPath + paramOption("redirect").getOrElse("/index.html")
  }

  def ssoContextPath: String = "/omatsivut"
}
