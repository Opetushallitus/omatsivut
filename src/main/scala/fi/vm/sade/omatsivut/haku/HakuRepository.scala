package fi.vm.sade.omatsivut.haku

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.ComponentRegistry
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.haku.domain.{Haku, HakuAika}
import fi.vm.sade.omatsivut.ohjausparametrit.OhjausparametritService
import fi.vm.sade.omatsivut.util.Timer

import scala.collection.JavaConversions._

case class HakuRepository(implicit val appConfig: AppConfig) extends Timer {
  private val repository = appConfig.springContext.applicationSystemService
  private val ohjausparametrit: OhjausparametritService = ComponentRegistry.ohjausparametritService
  private val koulutusinformaatioService = ComponentRegistry.koulutusInformaatioService

  def getHakuByApplication(application: Application)(implicit lang: Language.Language): Option[(ApplicationSystem, Haku)] = {
    application.getApplicationSystemId match {
      case "" => None
      case applicationSystemId =>
        tryFind(applicationSystemId).map(appSystem => (appSystem, HakuConverter.convertToHaku(appSystem)) match {
          case (appSystem, haku) => {
            val results = timed({
              ohjausparametrit.valintatulokset(applicationSystemId)
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

  def getApplicationPeriods(application: Application, applicationSystem: ApplicationSystem) = {
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
      .flatMap(koulutusinformaatioService.koulutus(_))
      .flatMap { koulutus => (koulutus.applicationStartDate, koulutus.applicationEndDate) match {
        case (Some(start), Some(end)) =>
          Some(List(HakuAika(start, end)))
        case _ =>
          None
      }
    }
  }
}

