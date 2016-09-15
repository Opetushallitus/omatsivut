package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.hakemuseditori.hakemus.SpringContextComponent
import fi.vm.sade.utils.slf4j.Logging
import org.slf4j.LoggerFactory

trait AuditLoggerComponent {
  this: SpringContextComponent =>

  val auditLogger: AuditLogger
  val auditContext: AuditContext

  class AuditLoggerFacade extends AuditLogger {
    protected val auditLog4jLogger = LoggerFactory.getLogger("audit")

    def log(event: AuditEvent) {
      auditLog4jLogger.info(event.toLogMessage)
    }
  }
}

trait AuditLogger extends Logging {
  def log(event: AuditEvent)
}

