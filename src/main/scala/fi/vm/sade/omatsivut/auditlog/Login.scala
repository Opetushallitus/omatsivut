package fi.vm.sade.omatsivut.auditlog

import javax.servlet.http.HttpServletRequest
import fi.vm.sade.auditlog.{Changes, Target, User}
import fi.vm.sade.hakemuseditori.auditlog.{AuditEvent, AuditLogUtils, OmatSivutMessageField, OmatSivutOperation}
import fi.vm.sade.omatsivut.security.SessionInfoRetriever.{getOppijaNumero, getSessionId}

case class Login(request: HttpServletRequest) extends AuditLogUtils with AuditEvent {
  override val operation: OmatSivutOperation = OmatSivutOperation.LOGIN
  override val changes: Changes = new Changes.Builder().build()
  override val target: Target = {
    new Target.Builder()
      .setField(OmatSivutMessageField.MESSAGE, "Käyttäjä kirjautui sisään")
      .build()
  }
  override def user: User = {
    val oppijaNumero = getOppijaNumero(request)
    val oid = oppijaNumero match {
      case Some(oppija) if oppija != "" => getOid(oppija)
      case _ => null
    }
    new User(oid, getAddress(request), getSessionId(request).getOrElse("(no session cookie)"), getUserAgent(request))
  }
}
