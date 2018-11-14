package fi.vm.sade.omatsivut.security

trait CookieNames {
  def oppijaNumeroCookieName = "oppijaNumero"
  def sessionCookieName = "session"
}

case class AuthenticationInfo(oppijaNumero: Option[String], sessionId: Option[String]) extends CookieNames {
  override def toString = oppijaNumeroCookieName + "=" + oppijaNumero.getOrElse("<not found>") +
              ", " + sessionCookieName + "=" + sessionId.getOrElse("<not found>")
}
