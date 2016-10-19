package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.hakemuseditori.valintatulokset.domain.VastaanottoAction

case class SaveVastaanotto(userOid: String, hakemusOid: String, hakukohdeOid: String, hakuOid: String, vastaanotto: VastaanottoAction) extends AuditEvent {
  def toLogMessage = "Tallennettu vastaanottotieto haussa " + hakuOid + ": " + vastaanotto + " oppijan " + userOid + " hakemuksen " + hakemusOid + " hakukohteen " + hakukohdeOid
}
