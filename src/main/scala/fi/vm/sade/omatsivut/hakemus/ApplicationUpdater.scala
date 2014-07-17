package fi.vm.sade.omatsivut.hakemus

import java.util.Date

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.omatsivut.domain.Hakemus
import scala.collection.JavaConversions._

object ApplicationUpdater {
  val hakutoiveetPhase: String = "hakutoiveet"

  def update(application: Application, hakemus: Hakemus) {
    updateHakutoiveet(application, hakemus)
    updateAllOtherPhases(application, hakemus)
    application.setUpdated(new Date())
  }

  private def updateAllOtherPhases(application: Application, hakemus: Hakemus) {
    val allOtherPhaseAnswers = hakemus.answers.filterKeys(phase => phase != hakutoiveetPhase)
    allOtherPhaseAnswers.foreach { case (phase, answers) =>
      val existingAnswers = application.getPhaseAnswers(phase)
      application.addVaiheenVastaukset(phase, existingAnswers ++ answers)
    }
  }

  private def updateHakutoiveet(application: Application, hakemus: Hakemus) {
    val updatedAnswers: Map[String, String] = HakutoiveetConverter.updateAnswers(hakemus, application.getPhaseAnswers(hakutoiveetPhase).toMap)
    application.addVaiheenVastaukset(hakutoiveetPhase, updatedAnswers)
  }
}
