package fi.vm.sade.omatsivut.haku

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.config.SpringContextComponent
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.haku.domain.{Haku, HakuAika}
import fi.vm.sade.omatsivut.koulutusinformaatio.{KoulutusInformaatioService, KoulutusInformaatioComponent}
import fi.vm.sade.omatsivut.ohjausparametrit.{OhjausparametritComponent, OhjausparametritService}
import fi.vm.sade.omatsivut.util.Timer

import scala.collection.JavaConversions._

trait HakuRepositoryComponent {
  this: OhjausparametritComponent with KoulutusInformaatioComponent with SpringContextComponent =>

  val ohjausparametritService: OhjausparametritService
  val koulutusInformaatioService: KoulutusInformaatioService

  class RemoteHakuRepository extends Timer with HakuRepository {
    private val repository = springContext.applicationSystemService

    def getHakuByApplication(application: Application)(implicit lang: Language.Language): Option[(ApplicationSystem, Haku)] = {
      application.getApplicationSystemId match {
        case "" => None
        case applicationSystemId =>
          tryFind(applicationSystemId).map(appSystem => (appSystem, HakuConverter.convertToHaku(appSystem)) match {
            case (appSystem, haku) => {
              val results = timed({
                ohjausparametritService.valintatulokset(applicationSystemId)
              }, 1000, "Ohjausparametrit valintatulokset")
              (appSystem, haku.copy(results = results, applicationPeriods = getApplicationPeriods(application, appSystem)))
            }
          })
      }
    }

    private def tryFind(applicationSystemOid: String): Option[ApplicationSystem] = {
      try {
        Some(timed({
          repository.getApplicationSystem(applicationSystemOid)
        }, 1000, "Application system service"))
      } catch {
        case e: Exception =>
          logger.error("applicationSystem loading failed", e)
          None
      }
    }

    def getApplicationPeriods(application: Application, applicationSystem: ApplicationSystem): List[HakuAika] = {
      val applicationPeriods = applicationSystem.getApplicationSystemType match {
        case OppijaConstants.LISA_HAKU =>
          getHakutoiveApplicationPeriods(application, applicationSystem)
        case _ => None
      }
      applicationPeriods.getOrElse(HakuConverter.convertToHakuajat(applicationSystem))
    }

    private def getHakutoiveApplicationPeriods(application: Application, applicationSystem: ApplicationSystem) : Option[List[HakuAika]] = {
      val hakutoiveet = application.getPhaseAnswers("hakutoiveet").toMap
      hakutoiveet.get("preference1-Koulutus-id")
        .flatMap(koulutusInformaatioService.koulutus(_, Language.fi.toString()))
        .flatMap { koulutus => (koulutus.applicationStartDate, koulutus.applicationEndDate) match {
        case (Some(start), Some(end)) =>
          Some(List(HakuAika(start, end)))
        case _ =>
          None
      }
      }
    }
  }
}

trait HakuRepository {
  def getHakuByApplication(application: Application)(implicit lang: Language.Language): Option[(ApplicationSystem, Haku)]
  def getApplicationPeriods(application: Application, applicationSystem: ApplicationSystem): List[HakuAika]
}

