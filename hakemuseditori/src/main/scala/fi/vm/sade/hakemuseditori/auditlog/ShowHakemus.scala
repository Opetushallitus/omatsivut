package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.log.model.Tapahtuma

case class ShowHakemus(userOid: String, hakemusOid: String, hakuOid: String, target: String = "Hakemus") extends AuditEvent {
  def toTapahtuma(context: AuditContext) = Tapahtuma.createREAD(context.systemName, userOid, target, toLogMessage)
  def toLogMessage = "Haettu haun " + hakuOid + " hakemus: " + hakemusOid + ", oppija " + userOid
}
