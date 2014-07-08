package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut._

object HakemusRepository extends Logging {
  def updateHakemus(hakemus: Hakemus) {
    ApplicationDaoWrapper.updateApplication(hakemus)
  }

  def fetchHakemukset(oid: String): List[Hakemus] = {
    ApplicationDaoWrapper.findByPersonOid(oid)
  }
}
