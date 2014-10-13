package fi.vm.sade.omatsivut.haku

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.config.SpringContextComponent
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.koulutusinformaatio.{KoulutusInformaatioComponent, KoulutusInformaatioService}
import fi.vm.sade.omatsivut.ohjausparametrit.{OhjausparametritComponent, OhjausparametritService}
import fi.vm.sade.omatsivut.tarjonta.{Hakuaika, Haku, TarjontaComponent, TarjontaService}
import fi.vm.sade.omatsivut.util.Logging
import fi.vm.sade.omatsivut.util.Timer.timed

trait HakuRepositoryComponent {
  this: OhjausparametritComponent with KoulutusInformaatioComponent with SpringContextComponent with TarjontaComponent =>

  val ohjausparametritService: OhjausparametritService
  val koulutusInformaatioService: KoulutusInformaatioService
  val tarjontaService: TarjontaService

  class RemoteHakuRepository extends HakuRepository with Logging {
    private val repository = springContext.applicationSystemService

    def getHakuByApplication(application: Application)(implicit lang: Language.Language): Option[(ApplicationSystem, Haku)] = {
      application.getApplicationSystemId match {
        case "" => None
        case applicationSystemId =>
          tryFind(applicationSystemId).map(appSystem => (appSystem, tarjontaService.haku(applicationSystemId)) match {
            case (as, Some(haku)) => (as, haku)
          })
      }
    }

    private def tryFind(applicationSystemOid: String): Option[ApplicationSystem] = {
      try {
        Some(timed(1000, "Application system service"){
          repository.getApplicationSystem(applicationSystemOid)
        })
      } catch {
        case e: Exception =>
          logger.error("applicationSystem loading failed", e)
          None
      }
    }

    override def getApplicationPeriods(applicationSystemId: String): List[Hakuaika] = {
      tarjontaService.haku(applicationSystemId).map(h => h.hakuajat) match {
        case Some(hakuajat) => hakuajat
        case _ => List()
      }
    }
  }
}

trait HakuRepository {
  def getHakuByApplication(application: Application)(implicit lang: Language.Language): Option[(ApplicationSystem, Haku)]
  def getApplicationPeriods(applicationSystemId: String): List[Hakuaika]
}

