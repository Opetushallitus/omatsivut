package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.hakemuseditori.auditlog.Operation.Operation

trait AuditEvent {
  def isUserOppija: Boolean
  def toLogMessage: Map[String, String]
  def operation: Operation
}

trait AuditContext {
  def systemName: String
}