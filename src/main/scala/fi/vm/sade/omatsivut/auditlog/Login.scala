package fi.vm.sade.omatsivut.auditlog

import fi.vm.sade.hakemuseditori.auditlog.AuditEvent
import fi.vm.sade.omatsivut.security.AuthenticationInfo

case class Login(authInfo: AuthenticationInfo, target: String = "Session") extends AuditEvent {
  def isUserOppija = true
  def toLogMessage = {
    val shib = authInfo.shibbolethCookie
    Map(
      "message" -> "Käyttäjä kirjautui sisään",
      "user" -> authInfo.personOid.getOrElse(""),
      "shibboleth" -> shib.map(_.toString).getOrElse("(no shibboleth cookie)"))
  }
}
