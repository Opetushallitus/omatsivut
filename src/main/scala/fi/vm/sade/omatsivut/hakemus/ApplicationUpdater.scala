package fi.vm.sade.omatsivut.hakemus

import java.util.Date

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.domain.Hakemus

import scala.collection.JavaConversions._

object ApplicationUpdater {
  val preferencePhaseKey = OppijaConstants.PHASE_APPLICATION_OPTIONS

  def update(application: Application, hakemus: Hakemus) {
    updateHakutoiveet(application, hakemus)
    updateAllOtherPhases(application, hakemus)
    application.setUpdated(new Date(hakemus.updated))
  }

  private def updateAllOtherPhases(application: Application, hakemus: Hakemus) {
    val allOtherPhaseAnswers = hakemus.answers.filterKeys(phase => phase != preferencePhaseKey)
    allOtherPhaseAnswers.foreach { case (phase, answers) =>
      val existingAnswers = application.getPhaseAnswers(phase)
      application.addVaiheenVastaukset(phase, existingAnswers ++ answers)
    }
  }

  private def updateHakutoiveet(application: Application, hakemus: Hakemus) {
    val updatedAnswers: Map[String, String] = HakutoiveetConverter.updateAnswers(hakemus, application.getPhaseAnswers(preferencePhaseKey).toMap)
    application.addVaiheenVastaukset(preferencePhaseKey, updatedAnswers)
  }
}
