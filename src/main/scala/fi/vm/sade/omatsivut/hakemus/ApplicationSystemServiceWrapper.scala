package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.domain.Haku

protected case class ApplicationSystemServiceWrapper(implicit val appConfig: AppConfig) {
  private val repository = appConfig.springContext.applicationSystemService

  def findByOid(applicationSystemOid: String): Option[Haku] = {
    tryFind(applicationSystemOid).map(HakuConverter.convertToHaku)
  }

  private def tryFind(applicationSystemOid: String): Option[ApplicationSystem] = {
    try {
      Some(repository.getApplicationSystem(applicationSystemOid))
    } catch {
      case e: Exception => None
    }
  }
}
