package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.auditlog.Operation

sealed case class OmatSivutOperation(value: String) extends Operation {
  override def name(): String = value
}

object OmatSivutOperation {
  object VIEW_HAKEMUS extends OmatSivutOperation("VIEW_HAKEMUS")
  object VIEW_HAKEMUS_WITH_VALIDATION extends OmatSivutOperation("VIEW_HAKEMUS_WITH_VALIDATION")
  object UPDATE_HAKEMUS extends OmatSivutOperation("UPDATE_HAKEMUS")
  object LOGIN extends OmatSivutOperation("LOGIN")
  object LOGOUT extends OmatSivutOperation("LOGOUT")
  object FETCH_TULOSKIRJE extends OmatSivutOperation("FETCH_TULOSKIRJE")
  object SAVE_VASTAANOTTO extends OmatSivutOperation("SAVE_VASTAANOTTO")
  object SAVE_ILMOITTAUTUMINEN extends OmatSivutOperation("SAVE_ILMOITTAUTUMINEN")

  val values = Seq(VIEW_HAKEMUS, VIEW_HAKEMUS_WITH_VALIDATION, UPDATE_HAKEMUS, LOGIN, LOGOUT, FETCH_TULOSKIRJE, SAVE_VASTAANOTTO, SAVE_ILMOITTAUTUMINEN)
}
