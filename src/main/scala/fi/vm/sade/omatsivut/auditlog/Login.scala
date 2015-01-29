package fi.vm.sade.omatsivut.auditlog

import fi.vm.sade.hakemuseditori.auditlog.{AuditContext, AuditEvent}
import fi.vm.sade.log.model.Tapahtuma
import fi.vm.sade.omatsivut.security.AuthenticationInfo

case class Login(authInfo: AuthenticationInfo, target: String = "Session") extends AuditEvent {
  def toTapahtuma(context: AuditContext) = Tapahtuma.createTRACE(context.systemName, target, toLogMessage, System.currentTimeMillis())
  def toLogMessage = "Käyttäjä kirjautui sisään: " + authInfo.toString
}
