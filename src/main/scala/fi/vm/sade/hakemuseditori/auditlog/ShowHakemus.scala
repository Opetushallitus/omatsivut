package fi.vm.sade.hakemuseditori.auditlog

import java.net.InetAddress

import fi.vm.sade.auditlog.{Changes, Target, User}

case class ShowHakemus(userOid: String, hakemusOid: String, hakuOid: String) extends AuditLogUtils with AuditEvent {
  override val operation: OmatSivutOperation = OmatSivutOperation.VIEW_HAKEMUS
  override val changes: Changes = new Changes.Builder().build()
  override val target: Target = {
    new Target.Builder()
      .setField(OmatSivutMessageField.MESSAGE, "Haettu haun hakemus")
      .setField(OmatSivutMessageField.HAKU_OID, hakuOid)
      .setField(OmatSivutMessageField.HAKEMUS_OID, hakemusOid)
      .build()
  }

  override def user: User = {
    new User(getOid(userOid).orNull, InetAddress.getLocalHost, "", "")
  }
}