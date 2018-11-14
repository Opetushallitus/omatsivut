package fi.vm.sade.omatsivut.security

class AuthenticationFailedException(msg: String, cause: Throwable) extends RuntimeException(msg, cause) {
  def this(msg: String) = this(msg, null)
  def this() = this(null, null)
}
