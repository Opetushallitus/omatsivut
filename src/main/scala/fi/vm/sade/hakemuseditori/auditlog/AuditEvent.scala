package fi.vm.sade.hakemuseditori.auditlog

trait OpiskelijaEvent {

}

trait AuditEvent {
  def toLogMessage: Map[String, String]
}

trait AuditContext {
  def systemName: String
}