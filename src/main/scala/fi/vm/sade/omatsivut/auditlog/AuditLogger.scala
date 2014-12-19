package fi.vm.sade.omatsivut.auditlog

import java.util.concurrent.ArrayBlockingQueue

import fi.vm.sade.omatsivut.config.SpringContextComponent
import fi.vm.sade.utils.slf4j.Logging
import org.slf4j.LoggerFactory

trait AuditLoggerComponent {
  this: SpringContextComponent =>

  val auditLogger: AuditLogger

  class AuditLoggerFacade(runningLogger: RunnableLogger) extends AuditLogger {
    protected val auditLog4jLogger = LoggerFactory.getLogger("audit")

    def log(event: AuditEvent) {
      runningLogger.log(event)
      auditLog4jLogger.info(event.toLogMessage)
    }
  }

  class RunnableLogger extends Runnable with Logging {
    private val queue = new ArrayBlockingQueue[AuditEvent](2000)
    private def auditLogger = springContext.auditLogger

    def log(event: AuditEvent) {
      try {
        queue.add(event)
      } catch {
        case e: IllegalStateException => event.toLogMessage
      }
    }

    override def run() {
      while(true) {
        val event = queue.take()
        withErrorLogging {
          auditLogger.log(event.toTapahtuma)
        }("Could not write Logout to auditlog, message was: " + event.toLogMessage)
      }
    }
  }
}

trait AuditLogger extends Logging {
  def log(event: AuditEvent)
}

