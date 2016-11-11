package fi.vm.sade.hakemuseditori.auditlog

case class FetchTuloskirje(hakuOid: String, hakemusOid: String) extends AuditEvent {
  def isUserOppija = true
  def toLogMessage = Map(
    "hakemusOid" -> hakemusOid,
    "hakuOid" -> hakuOid,
    "message" -> "Haettu tuloskirje hakemukselle"
  )
}
