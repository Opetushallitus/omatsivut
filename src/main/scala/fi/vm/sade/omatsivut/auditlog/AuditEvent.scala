package fi.vm.sade.omatsivut.auditlog

import fi.vm.sade.log.model.Tapahtuma
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus.Answers
import fi.vm.sade.omatsivut.security.CookieCredentials

sealed trait AuditEvent {
  def target: String
  def systemName = "omatsivut"
  def toTapahtuma: Tapahtuma
  def toLogMessage: String
}

case class Login(credentials: CookieCredentials, target: String = "Session") extends AuditEvent {
  def toTapahtuma = Tapahtuma.createTRACE(systemName, target, toLogMessage, System.currentTimeMillis())
  def toLogMessage = "Luotu eväste sisällöllä: " + credentials.toString
}
case class Logout(credentials: CookieCredentials, target: String = "Session") extends AuditEvent {
  def toTapahtuma = Tapahtuma.createTRACE(systemName, target, toLogMessage, System.currentTimeMillis())
  def toLogMessage = "Käyttäjä kirjautui ulos: " + credentials.toString
}
case class SessionTimeout(credentials: CookieCredentials, target: String = "Session") extends AuditEvent {
  def toTapahtuma = Tapahtuma.createTRACE(systemName, target, toLogMessage, System.currentTimeMillis())
  def toLogMessage = "Poistettu eväste sisällöllä: " + credentials.toString
}
case class ShowHakemus(userOid: String, hakemusOid: String, target: String = "Hakemus") extends AuditEvent {
  def toTapahtuma = Tapahtuma.createREAD(systemName, userOid, target, toLogMessage)
  def toLogMessage = "Haettu hakemus: " + hakemusOid + " with " + userOid
}
case class UpdateHakemus(userOid: String, hakemusOid: String, originalAnswers: Answers, updatedAnswers: Answers, target: String = "Hakemus") extends AuditEvent {
  def toTapahtuma = {
    val tapahtuma = Tapahtuma.createUPDATE(systemName, userOid, target, toLogMessage)
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

  def toLogMessage = "Tallennettu päivitetty hakemus: " + hakemusOid + " with " + userOid
}
