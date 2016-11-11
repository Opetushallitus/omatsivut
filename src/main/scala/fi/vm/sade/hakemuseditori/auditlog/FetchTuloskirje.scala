package fi.vm.sade.hakemuseditori.auditlog

case class FetchTuloskirje(hakuOid: String, hakemusOid: String) extends AuditEvent {
  def toLogMessage = "Haettu haun " + hakuOid + " tuloskirje hakemukselle " + hakemusOid
}
