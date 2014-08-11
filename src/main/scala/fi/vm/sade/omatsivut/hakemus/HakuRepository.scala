package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.domain.Haku
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.ohjausparametrit.OhjausparametritService
import fi.vm.sade.omatsivut.Logging

case class HakuRepository(implicit val appConfig: AppConfig) extends Logging {
  private val repository = appConfig.springContext.applicationSystemService

  def getHakuById(id: String)(implicit lang: Language.Language): Option[Haku] = id match {
    case "" => None
    case applicationSystemId =>
      tryFind(applicationSystemId).map(HakuConverter.convertToHaku).map { haku =>
        val results = OhjausparametritService(appConfig).valintatulokset(applicationSystemId)
        haku.copy(results = results)
      }
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

