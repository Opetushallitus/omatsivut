package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.domain.Haku

case class HakuRepository(implicit val appConfig: AppConfig) {
  def getHakuById(id: String): Option[Haku] = id match {
    case "" => None
    case applicationSystemId => ApplicationSystemServiceWrapper().findByOid(applicationSystemId)
  }
}
