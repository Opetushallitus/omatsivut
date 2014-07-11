package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.domain.Haku

object HakuRepository {
  def getApplicationSystemById(hakuOid: String): Option[Haku] = {
    ApplicationSystemServiceWrapper.findByOid(hakuOid)
  }
}
