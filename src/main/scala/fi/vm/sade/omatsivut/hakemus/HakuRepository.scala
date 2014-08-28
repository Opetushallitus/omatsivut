package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.config.AppConfig
import AppConfig.AppConfig
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.hakemus.domain.Haku
import fi.vm.sade.omatsivut.ohjausparametrit.OhjausparametritService
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.util.Timer

case class HakuRepository(implicit val appConfig: AppConfig) extends Timer {
  private val repository = appConfig.springContext.applicationSystemService

  def getHakuById(id: String)(implicit lang: Language.Language): Option[(ApplicationSystem, Haku)] = id match {
    case "" => None
    case applicationSystemId =>
      tryFind(applicationSystemId).map(appSystem => (appSystem, HakuConverter.convertToHaku(appSystem)) match {
        case (appSystem, haku) => {
          val results = timed({
            OhjausparametritService(appConfig).valintatulokset(applicationSystemId)
          }, 1000, "Ohjausparametrit valintatulokset")
          (appSystem, haku.copy(results = results))
        }
      })
  }

  private def tryFind(applicationSystemOid: String): Option[ApplicationSystem] = {
    try {
      Some(repository.getApplicationSystem(applicationSystemOid))
    } catch {
      case e: Exception =>
        logger.error("applicationSystem loading failed", e)
        None
    }
  }
}

