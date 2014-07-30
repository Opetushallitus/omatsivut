package fi.vm.sade.omatsivut.auditlog

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.Logging

object AuditLogger extends Logging  {
  private def auditLogger(implicit appConfig: AppConfig) = appConfig.springContext.auditLogger

  def log(audit: AuditEvent)(implicit appConfig: AppConfig) {
    withErrorLogging {
      auditLogger.log(audit.toTapahtuma)
    }(audit.toLogMessage)
  }
}
