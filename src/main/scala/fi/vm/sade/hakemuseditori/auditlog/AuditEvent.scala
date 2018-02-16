package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.auditlog.{Changes, Target, User}

trait AuditEvent {
  val operation: OmatSivutOperation
  val target: Target
  val changes: Changes
  def user: User
}

object OmatSivutMessageField {
  final val MESSAGE: String = "message"
  final val HAKEMUS_OID: String = "hakemusOid"
  final val HAKUKOHDE_OID: String = "hakukohdeOid"
  final val HAKU_OID: String = "hakuOid"
  final val VASTAANOTTO: String = "vastaanotto"
}