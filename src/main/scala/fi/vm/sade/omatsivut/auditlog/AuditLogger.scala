package fi.vm.sade.omatsivut.auditlog

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.domain.Hakemus
import fi.vm.sade.omatsivut.domain.Hakemus.Answers
import fi.vm.sade.omatsivut.security.CookieCredentials

object AuditLogger extends Logging  {
  private def auditLogger(implicit appConfig: AppConfig) = appConfig.springContext.auditLogger

  def logCreateSession(credentials: CookieCredentials)(implicit appConfig: AppConfig) {
    val logMessage = Login(credentials)
    auditLog(logMessage)
  }

  def logUpdatedHakemus(userOid: String, applicationOid: String, originalAnswers: Answers, updatedAnswers: Answers)(implicit appConfig: AppConfig) {
    val logMessage = UpdateHakemus(userOid, applicationOid, originalAnswers, updatedAnswers)
    auditLog(logMessage)
  }

  def logFetchHakemus(userOid: String, hakemus: Hakemus)(implicit appConfig: AppConfig) {
    val logMessage = ShowHakemus(userOid, hakemus.oid)
    auditLog(logMessage)
  }

  def logSessionTimeout(credentials: CookieCredentials)(implicit appConfig: AppConfig) {
    val logMessage = SessionTimeout(credentials)
    auditLog(logMessage)
  }

  def logLogout(credentials: CookieCredentials)(implicit appConfig: AppConfig) {
    val logMessage = Logout(credentials)
    auditLog(logMessage)
  }

  def auditLog(audit: AuditEvent)(implicit appConfig: AppConfig) {
    withErrorLogging {
      auditLogger.log(audit.toTapahtuma)
    }(audit.toLogMessage)
  }
}
