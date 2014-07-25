package fi.vm.sade.omatsivut.auditlog

import fi.vm.sade.log.client.Logger
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.log.model.Tapahtuma
import java.util.Date
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.auditlog.AuditEventType._

class AuditLogger(implicit val appConfig: AppConfig) extends Logging  {
  private val auditLogger = appConfig.springContext.auditLogger;
  private val systemName = "omatsivut"
  
  def logEvent(userOid: String, eventType: AuditEventType, targetType: String, targetValue: String) : Unit = {
    withErrorLogging {
      val tapahtuma: Tapahtuma = eventType match {
        case Create => Tapahtuma.createCREATE(systemName, userOid, targetType, targetValue)
        case Read => Tapahtuma.createREAD(systemName, userOid, targetType, targetValue)
        case Delete => Tapahtuma.createDELETE(systemName, userOid, targetType, targetValue)
        case Update => Tapahtuma.createUPDATE(systemName, userOid, targetType, targetValue)
      }
      logger.debug(tapahtuma.toString());
      auditLogger.log(tapahtuma);
    }("Could not log " + eventType + " " + targetType + " event for " + userOid)
  }
}