package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.auditlog.omatsivut.LogMessage._
import fi.vm.sade.auditlog.{ApplicationType, Audit}
import fi.vm.sade.hakemuseditori.hakemus.SpringContextComponent
import fi.vm.sade.utils.slf4j.Logging
import scala.collection.JavaConversions._


trait AuditLoggerComponent {
  this: SpringContextComponent =>

  val auditLogger: AuditLogger
  val auditContext: AuditContext

  class AuditLoggerFacade extends AuditLogger {

    def log(event: AuditEvent) {
      val msg = new LogMessageBuilder().addAll(event.toLogMessage).build()
      AuditWrapper.audit.log(msg)
    }
  }
}

trait AuditLogger extends Logging {
  def log(event: AuditEvent)
}

object AuditWrapper {
  val audit = new Audit("omatsivut", ApplicationType.OPISKELIJA)
}
