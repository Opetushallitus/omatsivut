package fi.vm.sade.omatsivut.security

case class ShibbolethCookie(name: String, value: String) {
  override def toString = name + "=" + value
}
