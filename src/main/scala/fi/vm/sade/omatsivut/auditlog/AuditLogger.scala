package fi.vm.sade.omatsivut.auditlog

import fi.vm.sade.log.client.Logger
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.log.model.Tapahtuma
import java.util.Date
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.security.ShibbolethCookie

class AuditLogger(implicit val appConfig: AppConfig) extends Logging  {
  private val auditLogger = appConfig.springContext.auditLogger;
  private val systemName = "omatsivut"
  
  def logCreateSession(userOid: String, cookie: ShibbolethCookie) : Unit = {
    withErrorLogging {
      val tapahtuma = Tapahtuma.createCREATE(systemName, userOid, cookie.name, cookie.value )
      logger.debug(tapahtuma.toString());
      auditLogger.log(tapahtuma);
    }("Could not logCreateSession for " + userOid)
  }
}