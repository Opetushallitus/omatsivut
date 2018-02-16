package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.auditlog.Operation

sealed case class OmatSivutOperation(value: String) extends Operation {
  override def name(): String = value
}

object OmatSivutOperation {
  object VIEW_HAKEMUS extends OmatSivutOperation("VIEW_HAKEMUS")
  object UPDATE_HAKEMUS extends OmatSivutOperation("UPDATE_HAKEMUS")
  object LOGIN extends OmatSivutOperation("LOGIN")
  object LOGOUT extends OmatSivutOperation("LOGOUT")
  object FETCH_TULOSKIRJE extends OmatSivutOperation("FETCH_TULOSKIRJE")
  object SAVE_VASTAANOTTO extends OmatSivutOperation("SAVE_VASTAANOTTO")

  val values = Seq(VIEW_HAKEMUS, UPDATE_HAKEMUS, LOGIN, LOGOUT, FETCH_TULOSKIRJE, SAVE_VASTAANOTTO)
}