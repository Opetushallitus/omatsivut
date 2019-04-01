package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.auditlog.{Changes, Target, User}
import fi.vm.sade.omatsivut.security.SessionInfoRetriever.getSessionId
import javax.servlet.http.HttpServletRequest


case class ShowHakemus(request: HttpServletRequest, userOid: String, hakemusOid: String, hakuOid: String) extends AuditLogUtils with AuditEvent {
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
    new User(getOid(userOid), getAddress(request), getSessionId(request).getOrElse("(no session cookie)"), getUserAgent(request))
  }
}
