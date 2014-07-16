package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.haku.testfixtures.MongoFixtureImporter
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.fixtures.FixtureUtility
import fi.vm.sade.omatsivut.security.AuthenticationInfoService
import org.scalatra.CookieOptions

class TestHelperServlet(implicit val appConfig: AppConfig) extends AuthCookieCreating  {
  if(appConfig.isTest){
    get("/fakesession") {
      val hetuOption: Option[String] = paramOption("hetu")
      fetchOid(hetuOption, AuthenticationInfoService.apply) match {
        case Some(oid) => new FixtureUtility().updateEmptySsnInApplications(oid, hetuOption.get)
        case _ =>
      }
      createAuthCookieResponse(hetuOption, CookieOptions(secure = false, path = "/"), paramOption("redirect").getOrElse("/index.html"))
    }

    put("/fixtures/apply") {
      MongoFixtureImporter.importJsonFixtures(appConfig.mongoTemplate)
    }
  }
}
