package fi.vm.sade.hakemuseditori.auditlog

case class ShowHakemus(userOid: String, hakemusOid: String, hakuOid: String, target: String = "Hakemus") extends AuditEvent {
  def toLogMessage = "Haettu haun " + hakuOid + " hakemus: " + hakemusOid + ", oppija " + userOid
}
