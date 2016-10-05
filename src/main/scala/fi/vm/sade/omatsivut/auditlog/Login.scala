package fi.vm.sade.omatsivut.auditlog

import fi.vm.sade.hakemuseditori.auditlog.AuditEvent
import fi.vm.sade.omatsivut.security.AuthenticationInfo

case class Login(authInfo: AuthenticationInfo, target: String = "Session") extends AuditEvent {
  def toLogMessage = "Käyttäjä kirjautui sisään: " + authInfo.toString
}
