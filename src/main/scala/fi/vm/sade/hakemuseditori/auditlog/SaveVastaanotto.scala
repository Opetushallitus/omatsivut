package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.hakemuseditori.valintatulokset.domain.VastaanottoAction

case class SaveVastaanotto(userOid: String, hakemusOid: String, hakukohdeOid: String, hakuOid: String, vastaanotto: VastaanottoAction) extends AuditEvent {
  def isUserOppija = true
  def toLogMessage = Map(
    "userOid" -> userOid,
    "hakemusOid" -> hakemusOid,
    "hakukohdeOid" -> hakukohdeOid,
    "hakuOid" -> hakuOid,
    "vastaanotto" -> vastaanotto.toString,
    "message" -> "Tallennettu vastaanottotieto haussa")

}
