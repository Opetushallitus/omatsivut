package fi.vm.sade.omatsivut.auditlog

import fi.vm.sade.log.model.Tapahtuma
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.domain.Hakemus
import fi.vm.sade.omatsivut.domain.Hakemus.Answers
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

  def logUpdatedHakemus(userOid: String, applicationOid: String, originalAnswers: Answers, updatedAnswers: Answers) {
    withErrorLogging {
      val tapahtuma = Tapahtuma.createUPDATE(systemName, userOid, "Hakemus", "Tallennettu päivitetty hakemus: " + applicationOid)
      val phaseIds = originalAnswers.keySet ++ updatedAnswers.keySet
      phaseIds foreach {
        phaseId => addPhaseAnswersDiff(tapahtuma, phaseId, originalAnswers.getOrElse(phaseId, Map()), updatedAnswers.getOrElse(phaseId, Map()))
      }
      auditLogger.log(tapahtuma)
    }("Could not logUpdatedHakemus for " + userOid)
  }

  private def addPhaseAnswersDiff(tapahtuma: Tapahtuma, phaseId: String, originalAnswers: Map[String,String], updatedAnswers: Map[String,String]) {
    val allKeys = originalAnswers.keySet ++ updatedAnswers.keySet
    allKeys foreach {
      key => {
        val oldValue = originalAnswers.get(key)
        val newValue = updatedAnswers.get(key)
        if(!oldValue.equals(newValue)) {
          tapahtuma.addValueChange("phase_" + phaseId + "-key_" + key, oldValue.getOrElse(null), newValue.getOrElse(null));
        }
      }
    }
  }

  def logFetchHakemus(userOid: String, hakemus: Hakemus) {
    withErrorLogging {
      val tapahtuma = Tapahtuma.createREAD(systemName, userOid, "Hakemus", "Haettu hakemus: " + hakemus.oid )
      auditLogger.log(tapahtuma)
    }("Could not logFetchHakemus for " + userOid)
  }
}