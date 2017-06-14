package fi.vm.sade.hakemuseditori.auditlog
import fi.vm.sade.hakemuseditori.auditlog.Operation.Operation

case class ShowHakemus(userOid: String, hakemusOid: String, hakuOid: String, target: String = "Hakemus") extends AuditEvent {
  def isUserOppija = true
  def toLogMessage = Map(
    "message" ->"Haettu haun hakemus",
    "hakuOid" -> hakuOid,
    "hakemus" -> hakemusOid,
    "oppija" -> userOid,
    "id" -> userOid)

  override def operation: Operation = Operation.VIEW
}
