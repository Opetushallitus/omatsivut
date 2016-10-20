package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori.hakemus.SpringContextComponent
import fi.vm.sade.haku.oppija.hakemus.domain.dto.SyntheticApplication
import fi.vm.sade.haku.oppija.hakemus.domain.dto.SyntheticApplication.Hakemus
import fi.vm.sade.haku.testfixtures.MongoFixtureImporter
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.fixtures.hakemus.{ErillishakuFixtureImporter, ApplicationFixtureImporter}
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaComponent
import fi.vm.sade.utils.Timer
import fi.vm.sade.hakemuseditori.valintatulokset.{FailingRemoteValintatulosService, RemoteValintatulosService, ValintatulosServiceComponent}
import org.scalatra.{InternalServerError, Ok}

trait FixtureServletContainer {
  this: ValintatulosServiceComponent with SpringContextComponent with TarjontaComponent =>

  def newFixtureServlet: FixtureServlet

  class FixtureServlet(val appConfig: AppConfig) extends OmatSivutServletBase  {
    if(appConfig.usesLocalDatabase) {
      put("/fixtures/erillishaku") {
        val hyvaksytty: Boolean = params("hyvaksytty").toBoolean
        new ErillishakuFixtureImporter(appConfig, springContext).applyFixtures(hyvaksytty)
        Ok
      }
      put("/fixtures/hakemus/apply") {
        val fixtureName: String = params("fixturename")
        val applicationOid: String = params.get("applicationOid").getOrElse("*").split("\\.").last
        Timer.timed("Apply fixtures", 100){
          new ApplicationFixtureImporter(springContext).applyFixtures(fixtureName, "application/"+applicationOid+".json")
        }
        Ok
      }

      put("/fixtures/valintatulos/apply") {
        val query = request.queryString
        new RemoteValintatulosService().applyFixtureWithQuery(request.multiParameters)
        Ok
      }

      put("/fixtures/valintatulos/fail/:value") {
        valintatulosService match {
          case s: FailingRemoteValintatulosService =>
            s.shouldFail = params("value").toBoolean
            Ok
          case _ => InternalServerError
        }
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

      put("/fixtures/haku/:oid/overrideHakuKierrosPaattyy/:timestamp") {
        tarjontaService match {
          case service: StubbedTarjontaService => {
            service.modifyHakukierrosPaattyy(params("oid"), params("timestamp").toLong)
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

      put("/fixtures/haku/:oid/resetHakuPaattyy") {
        tarjontaService match {
          case service: StubbedTarjontaService => {
            service.resetHakukierrosPaattyy(params("oid"))
            Ok
          }
          case _ => InternalServerError
        }
      }


    }
  }
}

