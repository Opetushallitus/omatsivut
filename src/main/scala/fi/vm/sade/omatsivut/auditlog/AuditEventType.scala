package fi.vm.sade.omatsivut.auditlog

object AuditEventType extends Enumeration {
  type AuditEventType = Value
  val Create, Read, Delete, Update = Value
}