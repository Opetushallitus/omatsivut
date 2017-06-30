package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.auditlog.omatsivut.LogMessage._
import fi.vm.sade.auditlog.{ApplicationType, Audit}
import fi.vm.sade.hakemuseditori.hakemus.SpringContextComponent
import fi.vm.sade.utils.slf4j.Logging


trait AuditLoggerComponent {
  this: SpringContextComponent =>

  val auditLogger: AuditLogger
  val auditContext: AuditContext

  class AuditLoggerFacade extends AuditLogger {

    private val audit = new Audit("omatsivut", ApplicationType.OPISKELIJA)

    def log(event: AuditEvent) {
      var logs: Map[String, String] = event.toLogMessage
      logs = logs + ("operation" -> event.operation.toString)
      val msg = new LogMessageBuilder().addAll(scala.collection.JavaConversions.mapAsJavaMap(logs)).build()
      audit.log(msg)
    }

    override def logDiff(event: AuditEvent, diff: Iterable[DiffTriplet]): Unit = {
      var logs = event.toLogMessage
      logs = logs + ("operation" -> event.operation.toString)
      var msg = new LogMessageBuilder().addAll(scala.collection.JavaConversions.mapAsJavaMap(logs))
      diff.foreach(triplet => {
        msg = msg.add(triplet.key, triplet.newValue, triplet.oldValue)
      })
      val message = msg.build()
      audit.log(message)
    }
  }
}

trait AuditLogger extends Logging {
  def log(event: AuditEvent)
  def logDiff(event: AuditEvent, diff: Iterable[DiffTriplet])
}

/**
  * Used as parameter for logDiff function
  * @param key
  * @param oldValue
  * @param newValue
  */
private[hakemuseditori] case class DiffTriplet(key: String, oldValue: String, newValue: String)