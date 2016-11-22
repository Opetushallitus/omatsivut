package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.auditlog.haku.LogMessage
import fi.vm.sade.auditlog.{ApplicationType, Audit}
import fi.vm.sade.hakemuseditori.hakemus.SpringContextComponent
import fi.vm.sade.utils.slf4j.Logging
import scala.collection.JavaConversions._


trait AuditLoggerComponent {
  this: SpringContextComponent =>

  val auditLogger: AuditLogger
  val auditContext: AuditContext

  class AuditLoggerFacade extends AuditLogger {
    private val (virkailija, opiskelija) = (new Audit("omatsivut", ApplicationType.VIRKAILIJA), new Audit("omatsivut", ApplicationType.OPISKELIJA))

    def log(event: AuditEvent) {
      virkailija.log(new LogMessage(event.toLogMessage))
    }
  }
}

trait AuditLogger extends Logging {
  def log(event: AuditEvent)
}

