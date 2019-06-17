package fi.vm.sade.hakemuseditori.auditlog

import javax.servlet.http.HttpServletRequest

import fi.vm.sade.auditlog.{Changes, Target}
import fi.vm.sade.hakemuseditori.valintatulokset.domain.Ilmoittautuminen

case class SaveIlmoittautuminen(request: HttpServletRequest, hakuOid: String, hakemusOid: String, ilmoittautuminen: Ilmoittautuminen, success: Boolean) extends AuditLogUtils with AuditEvent {
  override val operation: OmatSivutOperation = OmatSivutOperation.SAVE_ILMOITTAUTUMINEN
  override val changes: Changes = new Changes.Builder().build()
  override val target: Target = new Target.Builder()
    .setField(OmatSivutMessageField.MESSAGE, "Tallennettu ilmoittautuminen")
    .setField(OmatSivutMessageField.HAKEMUS_OID, hakemusOid)
    .setField(OmatSivutMessageField.HAKUKOHDE_OID, ilmoittautuminen.hakukohdeOid)
    .setField(OmatSivutMessageField.HAKU_OID, hakuOid)
    .setField(OmatSivutMessageField.ILMOITTAUTUMINEN, ilmoittautuminen.tila)
    .setField(OmatSivutMessageField.ILMOITTAUTUMINEN_SELITE, ilmoittautuminen.selite)
    .setField(OmatSivutMessageField.ILMOITTAUTUMINEN_SUCCESS, if (success) "kyllä" else "ei")
    .build()

  override def user = getUser(ilmoittautuminen.muokkaaja, request)
}
