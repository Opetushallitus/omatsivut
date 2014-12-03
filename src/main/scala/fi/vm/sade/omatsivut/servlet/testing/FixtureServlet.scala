package fi.vm.sade.omatsivut.servlet.testing

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.config.SpringContextComponent
import fi.vm.sade.omatsivut.fixtures.FixtureImporter
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import fi.vm.sade.omatsivut.tarjonta.TarjontaComponent
import fi.vm.sade.omatsivut.util.Timer
import fi.vm.sade.omatsivut.valintatulokset.{RemoteValintatulosService, ValintatulosServiceComponent}
import org.scalatra.{InternalServerError, Ok}

trait FixtureServletContainer {
  this: ValintatulosServiceComponent with SpringContextComponent with TarjontaComponent =>

  def newFixtureServlet: FixtureServlet

  class FixtureServlet(val appConfig: AppConfig) extends OmatSivutServletBase  {
    if(appConfig.usesLocalDatabase) {
      put("/fixtures/apply") {
        val fixtureName: String = params("fixturename")
        val applicationOid: String = params.get("applicationOid").getOrElse("*").split("\\.").last
        Timer.timed(100, "Apply fixtures"){
          new FixtureImporter(springContext.applicationDAO, springContext.mongoTemplate).applyFixtures(fixtureName, "application/"+applicationOid+".json")
        }
        Ok
      }

      put("/fixtures/valintatulos/apply") {
        val query = request.queryString
        new RemoteValintatulosService(appConfig.settings.valintaTulosServiceUrl).applyFixtureWithQuery(query)
        Ok
      }

      put("/fixtures/haku/:oid/overrideStart/:timestamp") {
        tarjontaService match {
          case service: StubbedTarjontaService => {
            service.modifyHaunAlkuaika(params("oid"), params("timestamp").toLong)
            Ok
          }
          case _ => InternalServerError
        }
      }

      put("/fixtures/haku/:oid/invertPriority") {
        tarjontaService match {
          case service: StubbedTarjontaService => {
            service.invertPriority(params("oid"))
            Ok
          }
          case _ => InternalServerError
        }
      }

      put("/fixtures/haku/:oid/resetPriority") {
        tarjontaService match {
          case service: StubbedTarjontaService => {
            service.resetPriority(params("oid"))
            Ok
          }
          case _ => InternalServerError
        }
      }

      put("/fixtures/haku/:oid/resetStart") {
        tarjontaService match {
          case service: StubbedTarjontaService => {
            service.resetHaunAlkuaika(params("oid"))
            Ok
          }
          case _ => InternalServerError
        }
      }
    }
  }
}

