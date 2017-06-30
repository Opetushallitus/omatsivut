package fi.vm.sade.hakemuseditori.auditlog
import fi.vm.sade.hakemuseditori.auditlog.Operation.Operation

case class FetchTuloskirje(id: String, hakuOid: String, hakemusOid: String) extends AuditEvent {
  override def isUserOppija = true
  override def toLogMessage = Map(
    "id" -> id,
    "hakemusOid" -> hakemusOid,
    "hakuOid" -> hakuOid,
    "message" -> "Haettu tuloskirje hakemukselle"
  )
  override def operation: Operation = Operation.FETCH_TULOSKIRJE
}
