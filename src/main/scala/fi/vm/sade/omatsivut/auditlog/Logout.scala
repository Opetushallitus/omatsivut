package fi.vm.sade.omatsivut.auditlog

import fi.vm.sade.hakemuseditori.auditlog.{AuditEvent, Operation}
import fi.vm.sade.hakemuseditori.auditlog.Operation.Operation
import fi.vm.sade.omatsivut.security.AuthenticationInfo

case class Logout(authInfo: AuthenticationInfo, target: String = "Session") extends AuditEvent {
  def isUserOppija = true
  def toLogMessage = Map("message" -> "Käyttäjä kirjautui ulos", "id" -> authInfo.toString)

  override def operation: Operation = Operation.LOGOUT
}
