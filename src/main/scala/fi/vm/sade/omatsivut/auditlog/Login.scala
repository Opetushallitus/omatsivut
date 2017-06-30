package fi.vm.sade.omatsivut.auditlog

import fi.vm.sade.hakemuseditori.auditlog.{AuditEvent, Operation}
import fi.vm.sade.hakemuseditori.auditlog.Operation.Operation
import fi.vm.sade.omatsivut.security.AuthenticationInfo

case class Login(authInfo: AuthenticationInfo, target: String = "Session") extends AuditEvent {
  def isUserOppija = true

  override def operation: Operation = Operation.LOGIN

  def toLogMessage = {
    val shib = authInfo.shibbolethCookie
    Map(
      "message" -> "Käyttäjä kirjautui sisään",
      "userId" -> authInfo.personOid.getOrElse(""),
      "userSession" -> shib.map(_.toString).getOrElse("(no shibboleth cookie)"))
  }
}
