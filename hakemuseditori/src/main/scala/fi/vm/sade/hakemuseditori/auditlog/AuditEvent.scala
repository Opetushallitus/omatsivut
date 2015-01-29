package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.log.model.Tapahtuma

trait AuditEvent {
  def target: String
  def toTapahtuma(context: AuditContext): Tapahtuma
  def toLogMessage: String
}

trait AuditContext {
  def systemName: String
}