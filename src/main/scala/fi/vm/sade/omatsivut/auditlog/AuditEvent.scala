package fi.vm.sade.omatsivut.auditlog

import fi.vm.sade.log.model.Tapahtuma
import fi.vm.sade.omatsivut.domain.Hakemus.Answers
import fi.vm.sade.omatsivut.security.CookieCredentials

sealed trait AuditEvent {
  def target: String
  def targetType: String
  def systemName = "omatsivut"
  def toTapahtuma: Tapahtuma
  def toLogMessage: String
}

case class Login(credentials: CookieCredentials, target: String = "Session") extends AuditEvent {
  def targetType = "Luotu eväste sisällöllä: " + credentials.toString
  def toTapahtuma = Tapahtuma.createTRACE(systemName, target, targetType, System.currentTimeMillis())
  def toLogMessage = "Could not write Logout to auditlog, message was: " + targetType
}
case class Logout(credentials: CookieCredentials, target: String = "Session") extends AuditEvent {
  def targetType = "Käyttäjä kirjautui ulos: " + credentials.toString
  def toTapahtuma = Tapahtuma.createTRACE(systemName, target, targetType, System.currentTimeMillis())
  def toLogMessage = "Could not write Logout to auditlog, message was: " + targetType
}
case class SessionTimeout(credentials: CookieCredentials, target: String = "Session") extends AuditEvent {
  def targetType = "Poistettu eväste sisällöllä: " + credentials.toString
  def toTapahtuma = Tapahtuma.createTRACE(systemName, target, targetType, System.currentTimeMillis())
  def toLogMessage = "Could not write SessionTimeout to auditlog, message was: " + targetType
}
case class ShowHakemus(userOid: String, hakemusOid: String, target: String = "Hakemus") extends AuditEvent {
  def targetType = "Haettu hakemus: " + hakemusOid
  def toTapahtuma = Tapahtuma.createREAD(systemName, userOid, target, targetType)
  def toLogMessage = "Could not write ShowApplication to auditlog, message was: " + targetType + " with " + userOid
}
case class UpdateHakemus(userOid: String, hakemusOid: String, originalAnswers: Answers, updatedAnswers: Answers, target: String = "Hakemus") extends AuditEvent {
  def targetType = "Tallennettu päivitetty hakemus: " + hakemusOid
  def toTapahtuma = {
    val tapahtuma = Tapahtuma.createUPDATE(systemName, userOid, target, targetType)
    val phaseIds = originalAnswers.keySet ++ updatedAnswers.keySet
    phaseIds foreach {
      phaseId => addPhaseAnswersDiff(tapahtuma, phaseId, originalAnswers.getOrElse(phaseId, Map()), updatedAnswers.getOrElse(phaseId, Map()))
    }
    tapahtuma
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

  def toLogMessage = "Could not write UpdateHakemus to auditlog, message was: " + targetType + " with " + userOid
}
