package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.hakemuseditori.auditlog.Operation.Operation
import fi.vm.sade.hakemuseditori.valintatulokset.domain.VastaanottoAction

case class SaveVastaanotto(userOid: String, hakemusOid: String, hakukohdeOid: String, hakuOid: String, vastaanotto: VastaanottoAction) extends AuditEvent {
  override def isUserOppija = true
  override def toLogMessage = Map(
    "id" -> userOid,
    "hakemusOid" -> hakemusOid,
    "hakukohdeOid" -> hakukohdeOid,
    "hakuOid" -> hakuOid,
    "vastaanotto" -> vastaanotto.toString,
    "message" -> "Tallennettu vastaanottotieto haussa")

  override def operation: Operation = Operation.UPDATE
}
