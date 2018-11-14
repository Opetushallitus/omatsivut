package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.auditlog.{Changes, Target, User}
import fi.vm.sade.omatsivut.security.AuthenticationInfoParser.getAuthenticationInfo
import javax.servlet.http.HttpServletRequest

case class FetchTuloskirje(request: HttpServletRequest, hakuOid: String, hakemusOid: String) extends AuditLogUtils with AuditEvent {
  override val operation: OmatSivutOperation = OmatSivutOperation.FETCH_TULOSKIRJE
  override val changes: Changes = new Changes.Builder().build()
  override val target: Target = new Target.Builder()
    .setField(OmatSivutMessageField.MESSAGE, "Haettu tuloskirje hakemukselle")
    .setField(OmatSivutMessageField.HAKU_OID, hakuOid)
    .setField(OmatSivutMessageField.HAKEMUS_OID, hakemusOid)
    .build()

  override def user: User = {
    val authInfo = getAuthenticationInfo(request)
    // oid (oppijaNumero) is also available in authInfo, should it be put in the User's constructor?
    new User(null, getAddress(request), authInfo.sessionId.getOrElse("(no session cookie)"), getUserAgent(request))
  }
}
