package fi.vm.sade.omatsivut.hakemus

import java.util.Date

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.omatsivut.domain.Hakemus
import scala.collection.JavaConversions._

object ApplicationUpdater {
  val hakutoiveetPhase: String = "hakutoiveet"
  val preferenceKeyPrefix: String = "preference"

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
    val hakutoiveet: Map[String, String] = application.getPhaseAnswers(hakutoiveetPhase).toMap
    val hakuToiveetWithEmptyValues = hakutoiveet.filterKeys(s => s.startsWith(preferenceKeyPrefix)).mapValues(s => "")
    val hakutoiveetWithoutOldPreferences = hakutoiveet.filterKeys(s => !s.startsWith(preferenceKeyPrefix))
    val hakutoiveetAnswers: Map[String, String] = hakemus.answers.getOrElse(hakutoiveetPhase, Map())
    val updatedHakutoiveet = hakutoiveetWithoutOldPreferences ++ hakuToiveetWithEmptyValues ++ convertHakutoiveet(hakemus.hakutoiveet) ++ hakutoiveetAnswers
    application.addVaiheenVastaukset(hakutoiveetPhase, updatedHakutoiveet)
  }

  def convertHakutoiveet(hakutoiveet: List[Hakemus.Hakutoive]): Map[String, String] = {
    hakutoiveet.zipWithIndex.flatMap {
      (t) => t._1.map {
        (elem) => (preferenceKeyPrefix + (t._2 + 1) + getDelimiter(elem._1) + elem._1, elem._2)
      }
    }.toMap[String, String]
  }

  private def getDelimiter(s: String) = if(s.contains("_")) "_" else "-"
}
