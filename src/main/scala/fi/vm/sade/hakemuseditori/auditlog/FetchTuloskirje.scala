package fi.vm.sade.hakemuseditori.auditlog

case class FetchTuloskirje(hakemusOid: String, hakuOid: String) extends AuditEvent {
  def toLogMessage = "Haettu haun " + hakuOid + " tuloskirje hakemukselle " + hakemusOid
}
