package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.hakemuseditori.valintatulokset.domain.Vastaanotto
import fi.vm.sade.log.model.Tapahtuma

case class SaveVastaanotto(userOid: String, hakemusOid: String, hakuOid: String, vastaanotto: Vastaanotto, target: String = "Vastaanottotila") extends AuditEvent {
  def toTapahtuma(context: AuditContext) = Tapahtuma.createUPDATE(context.systemName, userOid, target, toLogMessage)
  def toLogMessage = "Tallennettu vastaanottotieto haussa " + hakuOid + ": " + vastaanotto.tila + " oppijan " + userOid + " hakemuksen " + hakemusOid + " hakukohteen " + vastaanotto.hakukohdeOid
}
