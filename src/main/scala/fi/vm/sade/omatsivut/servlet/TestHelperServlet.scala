package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.fixtures.FixtureUtils
import org.scalatra.CookieOptions
import fi.vm.sade.omatsivut.security.AuthenticationInfoService

class TestHelperServlet(config: AppConfig)(implicit val authService: AuthenticationInfoService) extends AuthCookieCreating  {
  if(AppConfig.config.isTest){
    get("/fakesession") {
      createAuthCookieResponse(paramOption("hetu"), CookieOptions(secure = false, path = "/"), paramOption("redirect").getOrElse("/index.html"))
    }

    put("/fixtures/apply") {
      FixtureUtils.applyFixtures()
    }
  }
}
