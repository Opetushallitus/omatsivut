package fi.vm.sade.omatsivut

object SessionFailure extends Enumeration {
  type SessionFailure = Value
  val SESSION_NOT_FOUND, SESSION_EXPIRED = Value
}
