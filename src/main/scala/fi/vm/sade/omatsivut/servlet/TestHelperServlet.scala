package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.fixtures.FixtureImporter
import fi.vm.sade.omatsivut.security.AuthenticationInfoService
import org.scalatra.CookieOptions

class TestHelperServlet(implicit val appConfig: AppConfig) extends OmatSivutServletBase with AuthCookieCreating  {
  if(appConfig.isTest){
    get("/fakesession") {
      val hetuOption: Option[String] = paramOption("hetu")
      createAuthCookieCredentials(hetuOption, "placeholder", AuthenticationInfoService.apply) match {
        case Some(credentials) => createAuthCookieResponse(credentials, response, redirectUri)
        case _ => response.redirect(ssoContextPath + "/Shibboleth.sso/LoginFI") //TODO Localization
      }
    }

    put("/fixtures/apply") {
      FixtureImporter().applyFixtures
    }
  }

  def redirectUri: String = {
    paramOption("redirect").getOrElse("/index.html")
  }

  def ssoContextPath: String = "/omatsivut"
}
