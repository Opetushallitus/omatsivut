package fi.vm.sade.omatsivut.servlet.testing

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.config.SpringContextComponent
import fi.vm.sade.omatsivut.fixtures.FixtureImporter
import fi.vm.sade.omatsivut.security.{AuthenticationCipher, ShibbolethCookie}
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import fi.vm.sade.omatsivut.util.Timer
import fi.vm.sade.omatsivut.valintatulokset.ValintatulosServiceComponent
import org.scalatra.{Cookie, CookieOptions}

trait TestHelperServletContainer {
  this: ValintatulosServiceComponent with SpringContextComponent =>

  def newTestHelperServlet: TestHelperServlet

  class TestHelperServlet(val appConfig: AppConfig) extends OmatSivutServletBase  {
    if(appConfig.usesFakeAuthentication) {
      get("/fakesession") {
        val shibbolethCookie = ShibbolethCookie("_shibsession_fakeshibbolethsession", new AuthenticationCipher(appConfig.settings.aesKey, appConfig.settings.hmacKey).encrypt("FAKESESSION"))
        response.addCookie(fakeShibbolethSessionCookie(shibbolethCookie))
        paramOption("hetu") match {
          case Some(hetu) => response.redirect(request.getContextPath + "/secure/initsession?hetu=" + hetu)
          case _ => halt(400, "Can't fake session without ssn")
        }
      }
    }

    if(appConfig.usesLocalDatabase) {
      put("/fixtures/apply") {
        val fixtureName: String = params("fixturename")
        val applicationOid: String = params.get("applicationOid").getOrElse("*").split("\\.").last
        Timer.timed({
          new FixtureImporter(springContext.applicationDAO, springContext.mongoTemplate).applyFixtures(fixtureName, "application/"+applicationOid+".json")
        }, 100, "Apply fixtures")
      }
    }

    def fakeShibbolethSessionCookie(shibbolethSessionData: ShibbolethCookie): Cookie = {
      Cookie(shibbolethSessionData.name, shibbolethSessionData.value)(CookieOptions(path = "/"))
    }
  }
}

