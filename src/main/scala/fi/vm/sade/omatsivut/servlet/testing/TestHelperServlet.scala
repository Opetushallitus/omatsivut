package fi.vm.sade.omatsivut.servlet.testing

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.config.SpringContextComponent
import fi.vm.sade.omatsivut.fixtures.{FixtureImporter, ValintatulosFixtureImporter}
import fi.vm.sade.omatsivut.security.{AuthenticationCipher, ShibbolethCookie}
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import fi.vm.sade.omatsivut.valintatulokset.{MockValintatulosService, ValintatulosServiceComponent}
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
        new FixtureImporter(springContext.applicationDAO, springContext.mongoTemplate).applyFixtures(fixtureName)
      }
    }

    if(valintatulosService.isInstanceOf[MockValintatulosService]) {
      put("/fixtures/valintatulos") {
        val fixtureName: String = params("fixturename")
        new ValintatulosFixtureImporter(valintatulosService.asInstanceOf[MockValintatulosService]).applyFixtures(fixtureName)
      }
    }

    def fakeShibbolethSessionCookie(shibbolethSessionData: ShibbolethCookie): Cookie = {
      Cookie(shibbolethSessionData.name, shibbolethSessionData.value)(CookieOptions(path = "/"))
    }
  }
}

