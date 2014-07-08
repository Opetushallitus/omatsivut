package fi.vm.sade.omatsivut.hakemus

object HakuRepository {
  def getApplicationSystemById(hakuOid: String): Option[Haku] = {
    ApplicationSystemRepositoryWrapper.findByOid(hakuOid)
  }
}
