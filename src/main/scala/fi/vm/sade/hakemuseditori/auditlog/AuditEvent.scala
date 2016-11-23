package fi.vm.sade.hakemuseditori.auditlog

trait AuditEvent {
  def isUserOppija: Boolean
  def toLogMessage: Map[String, String]
}

trait AuditContext {
  def systemName: String
}