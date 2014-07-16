package fi.vm.sade.omatsivut.hakemus

import java.util.Date

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.omatsivut.domain.Hakemus
import scala.collection.JavaConversions._

object HakemusMerger {
  def merge(hakemus: Hakemus, application: Application) {
    mergeHakutoiveet(hakemus, application)
    mergeAllOtherPhases(hakemus, application)
    application.setUpdated(new Date())
  }

  private def mergeAllOtherPhases(hakemus: Hakemus, application: Application) {
    val allOtherPhaseAnswers = hakemus.answers.filterKeys(phase => phase == "hakutoiveet")
    allOtherPhaseAnswers.foreach { case (phase, answers) =>
      val existingAnswers = application.getPhaseAnswers(phase)
      application.addVaiheenVastaukset(phase, existingAnswers ++ answers)
    }
  }

  private def mergeHakutoiveet(hakemus: Hakemus, application: Application) {
    val hakutoiveet: Map[String, String] = application.getPhaseAnswers("hakutoiveet").toMap
    val hakuToiveetWithEmptyValues = hakutoiveet.filterKeys(s => s.startsWith("preference")).mapValues(s => "")
    val hakutoiveetWithoutOldPreferences = hakutoiveet.filterKeys(s => !s.startsWith("preference"))
    val hakutoiveetAnswers: Map[String, String] = hakemus.answers.get("hakutoiveet").getOrElse(Map())
    val updatedHakutoiveet = hakutoiveetWithoutOldPreferences ++ hakuToiveetWithEmptyValues ++ getUpdates(hakemus) ++ hakutoiveetAnswers
    application.addVaiheenVastaukset("hakutoiveet", updatedHakutoiveet)
  }

  private def getUpdates(hakemus: Hakemus): Map[String, String] = {
    hakemus.hakutoiveet.zipWithIndex.flatMap {
      (t) => t._1.map {
        (elem) => ("preference" + (t._2 + 1) + getDelimiter(elem._1) + elem._1, elem._2)
      }
    }.toMap[String, String]
  }

  private def getDelimiter(s: String) = if(s.contains("_")) "_" else "-"
}
