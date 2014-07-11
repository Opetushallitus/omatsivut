package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.domain.Haku

case class HakuRepository(implicit val appConfig: AppConfig) {
  def getApplicationSystemById(hakuOid: String): Option[Haku] = {
    ApplicationSystemServiceWrapper().findByOid(hakuOid)
  }
}
