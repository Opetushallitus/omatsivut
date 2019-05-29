package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.auditlog.{Changes, Target, User}
import javax.servlet.http.HttpServletRequest


case class ShowValidatedHakemus(request: HttpServletRequest, userOid: String, hakemusOid: String, hakuOid: String) extends AuditLogUtils with AuditEvent {
  override val operation: OmatSivutOperation = OmatSivutOperation.VIEW_HAKEMUS_WITH_VALIDATION
  override val changes: Changes = new Changes.Builder().build()
  override val target: Target = {
    new Target.Builder()
      .setField(OmatSivutMessageField.MESSAGE, "Haettu haun hakemus, validoitu ja palautettu hakemus muuttunein tiedoin")
      .setField(OmatSivutMessageField.HAKU_OID, hakuOid)
      .setField(OmatSivutMessageField.HAKEMUS_OID, hakemusOid)
      .build()
  }

  override def user = getUser(userOid, request)
}
