package fi.vm.sade.hakemuseditori.auditlog

trait AuditEvent {
  def toLogMessage: String
}

trait AuditContext {
  def systemName: String
}