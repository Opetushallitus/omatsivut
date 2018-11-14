package fi.vm.sade.omatsivut.auditlog

import javax.servlet.http.HttpServletRequest

import fi.vm.sade.auditlog.{Changes, Target, User}
import fi.vm.sade.hakemuseditori.auditlog.{AuditEvent, AuditLogUtils, OmatSivutMessageField, OmatSivutOperation}
import fi.vm.sade.omatsivut.security.AuthenticationInfoParser.getAuthenticationInfo

case class Login(request: HttpServletRequest) extends AuditLogUtils with AuditEvent {
  override val operation: OmatSivutOperation = OmatSivutOperation.LOGIN
  override val changes: Changes = new Changes.Builder().build()
  override val target: Target = {
    new Target.Builder()
      .setField(OmatSivutMessageField.MESSAGE, "Käyttäjä kirjautui sisään")
      .build()
  }
  override def user: User = {
    val authInfo = getAuthenticationInfo(request)
    new User(getOid(authInfo.oppijaNumero.get), getAddress(request), authInfo.sessionId.getOrElse("(no session cookie)"), getUserAgent(request))
  }
}
