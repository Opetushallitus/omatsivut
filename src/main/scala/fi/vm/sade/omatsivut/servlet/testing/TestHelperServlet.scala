package fi.vm.sade.omatsivut.servlet.testing

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.config.SpringContextComponent
import fi.vm.sade.omatsivut.fixtures.{FixtureImporter, TestFixture}
import fi.vm.sade.omatsivut.security.{AuthenticationCipher, FakeAuthentication, ShibbolethCookie}
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import fi.vm.sade.omatsivut.tarjonta.TarjontaComponent
import fi.vm.sade.omatsivut.util.Timer
import fi.vm.sade.omatsivut.valintatulokset.{RemoteValintatulosService, ValintatulosServiceComponent}
import org.scalatra.{Cookie, CookieOptions}

trait TestHelperServletContainer {
  this: ValintatulosServiceComponent with SpringContextComponent with TarjontaComponent =>

  def newTestHelperServlet: TestHelperServlet

  class TestHelperServlet(val appConfig: AppConfig) extends OmatSivutServletBase  {
    if(appConfig.usesFakeAuthentication) {
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
    }

    if(appConfig.usesLocalDatabase) {
      put("/fixtures/apply") {
        val fixtureName: String = params("fixturename")
        val applicationOid: String = params.get("applicationOid").getOrElse("*").split("\\.").last
        Timer.timed(100, "Apply fixtures"){
          new FixtureImporter(springContext.applicationDAO, springContext.mongoTemplate).applyFixtures(fixtureName, "application/"+applicationOid+".json")
        }
      }

      put("/fixtures/valintatulos/apply") {
        val query = request.queryString
        new RemoteValintatulosService(appConfig.settings.valintaTulosServiceUrl).applyFixtureWithQuery(query)
      }

      put("/fixtures/haku/:oid/overrideStart/:timestamp") {
        tarjontaService match {
          case service: StubbedTarjontaService =>
            service.modifyHaunAlkuaika(params("oid"), params("timestamp").toLong)
        }
      }

      put("/fixtures/haku/:oid/resetStart") {
        tarjontaService match {
          case service: StubbedTarjontaService =>
            service.resetHaunAlkuaika(params("oid"))
        }
      }
    }

    def fakeShibbolethSessionCookie(shibbolethSessionData: ShibbolethCookie): Cookie = {
      Cookie(shibbolethSessionData.name, shibbolethSessionData.value)(CookieOptions(path = "/"))
    }
  }
}

