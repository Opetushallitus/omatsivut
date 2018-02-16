package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.auditlog.{Changes, Target, User}
import fi.vm.sade.hakemuseditori.valintatulokset.domain.VastaanottoAction
import fi.vm.sade.omatsivut.security.AuthenticationInfoParser.getAuthenticationInfo

case class SaveVastaanotto(userOid: String, hakemusOid: String, hakukohdeOid: String, hakuOid: String, vastaanotto: VastaanottoAction) extends AuditLogUtils with AuditEvent {
  override val operation: OmatSivutOperation = OmatSivutOperation.SAVE_VASTAANOTTO
  override val changes: Changes = new Changes.Builder().build()
  override val target: Target = new Target.Builder()
    .setField(OmatSivutMessageField.MESSAGE, "Tallennettu vastaanottotieto haussa")
    .setField(OmatSivutMessageField.HAKEMUS_OID, hakemusOid)
    .setField(OmatSivutMessageField.HAKUKOHDE_OID, hakukohdeOid)
    .setField(OmatSivutMessageField.HAKU_OID, hakuOid)
    .setField(OmatSivutMessageField.VASTAANOTTO, vastaanotto.toString)
    .build()

  override def user: User = {
    new User(getOid(userOid).orNull, null, null, null)
  }
}
