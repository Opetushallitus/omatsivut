package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.domain.Haku

case class HakuRepository(implicit val appConfig: AppConfig) {
  private val repository = appConfig.springContext.applicationSystemService

  def getHakuById(id: String): Option[Haku] = id match {
    case "" => None
    case applicationSystemId => tryFind(applicationSystemId).map(HakuConverter.convertToHaku)
  }

  private def tryFind(applicationSystemOid: String): Option[ApplicationSystem] = {
    try {
      Some(repository.getApplicationSystem(applicationSystemOid))
    } catch {
      case e: Exception => None
    }
  }
}

