package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.fixtures.FixtureImporter
import org.scalatra.CookieOptions

class TestHelperServlet(implicit val appConfig: AppConfig) extends AuthCookieCreating  {
  if(appConfig.isTest){
    get("/fakesession") {
      val hetuOption: Option[String] = paramOption("hetu")
      createAuthCookieResponse(hetuOption, CookieOptions(secure = false, path = "/"), paramOption("redirect").getOrElse("/index.html"))
    }

    put("/fixtures/apply") {
      FixtureImporter().applyFixtures
    }
  }
}
