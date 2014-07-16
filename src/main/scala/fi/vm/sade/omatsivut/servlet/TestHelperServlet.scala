package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.haku.testfixtures.MongoFixtureImporter
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import org.scalatra.CookieOptions

class TestHelperServlet(config: AppConfig)(implicit val appConfig: AppConfig) extends AuthCookieCreating  {
  if(appConfig.isTest){
    get("/fakesession") {
      createAuthCookieResponse(paramOption("hetu"), CookieOptions(secure = false, path = "/"), paramOption("redirect").getOrElse("/index.html"))
    }

    put("/fixtures/apply") {
      MongoFixtureImporter.importJsonFixtures(appConfig.mongoTemplate)
    }
  }
}
