package fi.vm.sade.omatsivut.auditlog

import java.util.concurrent.ArrayBlockingQueue

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.Logging

object AuditLogger extends Logging {
  def logger(appConfig: AppConfig) = appConfig.auditLogger

  def log(event: AuditEvent)(implicit appConfig: AppConfig) {
    logger(appConfig).log(event)
  }
}

class RunnableLogger(val appConfig: AppConfig) extends Runnable with Logging {
  private val queue = new ArrayBlockingQueue[AuditEvent](2000)
  private def auditLogger = appConfig.springContext.auditLogger

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
      }(event.toLogMessage)
    }
  }
}