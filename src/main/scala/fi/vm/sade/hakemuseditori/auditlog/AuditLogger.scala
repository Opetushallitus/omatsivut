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

    private val audit = new Audit("omatsivut", ApplicationType.OPISKELIJA)

    def log(event: AuditEvent) {
      val msg = new LogMessageBuilder().addAll(event.toLogMessage).build()
      audit.log(msg)
    }

    override def logDiff(event: AuditEvent, diff: Iterable[(String, String, String)]): Unit = {
      var msg = new LogMessageBuilder().addAll(event.toLogMessage)
      diff.foreach(triplet => {
        msg = msg.add(triplet._1, triplet._3, triplet._2)
      })
      val message = msg.build()
      audit.log(message)
    }
  }
}

trait AuditLogger extends Logging {
  def log(event: AuditEvent)
  def logDiff(event: AuditEvent, diff: Iterable[(String, String, String)])
}
