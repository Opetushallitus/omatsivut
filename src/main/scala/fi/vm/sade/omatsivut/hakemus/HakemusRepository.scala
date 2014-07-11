package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut._
import fi.vm.sade.omatsivut.domain.Hakemus

case class HakemusRepository(implicit val appConfig: AppConfig) extends Logging {
  def updateHakemus(hakemus: Hakemus) {
    ApplicationDaoWrapper().updateApplication(hakemus)
  }

  def fetchHakemukset(oid: String): List[Hakemus] = {
    ApplicationDaoWrapper().findByPersonOid(oid)
  }
}
