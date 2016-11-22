package fi.vm.sade.hakemuseditori.auditlog

case class FetchTuloskirje(hakuOid: String, hakemusOid: String) extends AuditEvent {
  def isUserOppija = true
  def toLogMessage = Map("hakuOid" -> hakuOid, "hakemusOid" -> hakemusOid, "message" ->"Haettu haun tuloskirje hakemukselle")
}
