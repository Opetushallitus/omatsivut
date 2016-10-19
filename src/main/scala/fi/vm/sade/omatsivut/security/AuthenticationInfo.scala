package fi.vm.sade.omatsivut.security

case class AuthenticationInfo(personOid: Option[String], shibbolethCookie: Option[ShibbolethCookie]) {
  override def toString = "oid=" + personOid.getOrElse("") + ", " + shibbolethCookie.map(_.toString).getOrElse("(no shibboleth cookie)")
}
