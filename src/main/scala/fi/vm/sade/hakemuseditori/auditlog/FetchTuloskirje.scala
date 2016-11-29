package fi.vm.sade.hakemuseditori.auditlog

case class FetchTuloskirje(id: String, hakuOid: String, hakemusOid: String) extends AuditEvent {
  def isUserOppija = true
  def toLogMessage = Map(
    "id" -> id,
    "hakemusOid" -> hakemusOid,
    "hakuOid" -> hakuOid,
    "message" -> "Haettu tuloskirje hakemukselle"
  )
}
