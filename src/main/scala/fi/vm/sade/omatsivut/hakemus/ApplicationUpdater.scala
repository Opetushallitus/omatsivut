package fi.vm.sade.omatsivut.hakemus

import java.util.Date

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.domain.Hakemus

import scala.collection.JavaConversions._

object ApplicationUpdater {
  val preferencePhaseKey = OppijaConstants.PHASE_APPLICATION_OPTIONS

  def update(application: Application, hakemus: Hakemus) {
    val updatedAnswers = getUpdatedAnswersForApplication(application, hakemus)
    updatedAnswers.foreach { case (phaseId, phaseAnswers) =>
      application.addVaiheenVastaukset(phaseId, phaseAnswers)
    }
    application.setUpdated(new Date(hakemus.updated))
  }

  def getUpdatedAnswersForApplication(application: Application, hakemus: Hakemus): Map[String, Map[String, String]] = {
    updatedAnswersForHakuToiveet(application, hakemus) ++ updatedAnswersForOtherPhases(application, hakemus)
  }

  private def updatedAnswersForOtherPhases(application: Application, hakemus: Hakemus): Map[String, Map[String, String]] = {
    val allOtherPhaseAnswers = hakemus.answers.filterKeys(phase => phase != preferencePhaseKey)
    allOtherPhaseAnswers.map { case (phase, answers) =>
      val existingAnswers = application.getPhaseAnswers(phase).toMap
      (phase, existingAnswers ++ answers)
    }.toMap
  }

  private def updatedAnswersForHakuToiveet(application: Application, hakemus: Hakemus): Map[String, Map[String, String]] = {
    val updatedAnswers: Map[String, String] = HakutoiveetConverter.updateAnswers(hakemus, application.getPhaseAnswers(preferencePhaseKey).toMap)
    Map(preferencePhaseKey -> updatedAnswers)
  }
}
