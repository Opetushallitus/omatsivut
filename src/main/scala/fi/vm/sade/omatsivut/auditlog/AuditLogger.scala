package fi.vm.sade.omatsivut.auditlog

import fi.vm.sade.log.model.Tapahtuma
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.domain.Hakemus
import fi.vm.sade.omatsivut.security.ShibbolethCookie

class AuditLogger(implicit val appConfig: AppConfig) extends Logging  {
  private val auditLogger = appConfig.springContext.auditLogger
  private val systemName = "omatsivut"
  
  def logCreateSession(userOid: String, cookie: ShibbolethCookie) : Unit = {
    withErrorLogging {
      val tapahtuma = Tapahtuma.createCREATE(systemName, userOid, "Session", "Luotu sessio ShibbolethCookiella: " + cookie.toString )
      auditLogger.log(tapahtuma)
    }("Could not logCreateSession for " + userOid)
  }

  def logUpdatedHakemus(userOid: String, hakemus: Hakemus) {
    withErrorLogging {
      val tapahtuma = Tapahtuma.createUPDATE(systemName, userOid, "Hakemus", "Tallennettu päivitetty hakemus: " + hakemus.oid )
      auditLogger.log(tapahtuma)
    }("Could not logUpdatedHakemus for " + userOid)
  }
}