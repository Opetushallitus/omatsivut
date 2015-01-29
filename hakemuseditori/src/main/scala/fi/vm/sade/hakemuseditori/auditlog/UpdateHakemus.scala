package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.Answers
import fi.vm.sade.log.model.Tapahtuma

case class UpdateHakemus(userOid: String, hakemusOid: String, hakuOid: String, originalAnswers: Answers, updatedAnswers: Answers, target: String = "Hakemus") extends AuditEvent {
  def toTapahtuma(context: AuditContext) = {
    val tapahtuma = Tapahtuma.createUPDATE(context.systemName, userOid, target, toLogMessage)
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

  def toLogMessage = "Tallennettu päivitetty hakemus haussa " + hakuOid + ": " + hakemusOid + ", oppija " + userOid
}
