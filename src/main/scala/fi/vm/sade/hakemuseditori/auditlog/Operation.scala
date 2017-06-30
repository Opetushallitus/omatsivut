package fi.vm.sade.hakemuseditori.auditlog

object Operation extends Enumeration {
  type Operation = Value
  val VIEW, UPDATE, LOGIN, FETCH_TULOSKIRJE, LOGOUT = Value
}
