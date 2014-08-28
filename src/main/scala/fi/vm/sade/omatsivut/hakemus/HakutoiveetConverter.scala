package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus._

object HakutoiveetConverter {
  val preferenceKeyPrefix: String = "preference"
  val hakutoiveetPhase: String = OppijaConstants.PHASE_APPLICATION_OPTIONS
  val koulutusId: String = "Koulutus-id"

  def convertFromAnswers(answers: Answers): List[Hakutoive] = {
    groupPreferences(answers.getOrElse(hakutoiveetPhase , Map()))
      .toList
      .map(shortenNames)
      .map(convertEmptyPreferences)
      .sortBy(map => map.get("priority"))
      .map((m) => m.filterKeys { Set("priority").contains(_) == false})
  }

  def convertToAnswers(hakutoiveet: List[Hakutoive], answers: Answers): Map[String, String] = {
    val hakutoiveetAnswers = answers.getOrElse(hakutoiveetPhase, Map())
    hakutoiveetAnswers.filterKeys(s => !s.startsWith(HakutoiveetConverter.preferenceKeyPrefix)) ++
    hakutoiveet.zipWithIndex.flatMap {
      case (hakutoive, index) => {
        Map((longKey(koulutusId, index), "")) ++
        hakutoive.map {
          case (key, value) => (longKey(key, index), value)
        } ++ hakutoiveetAnswers.filterKeys(_.startsWith((preferenceKeyPrefix + (index + 1))))
      }
    }.toMap[String, String]
  }

  def updateAnswers(hakutoiveet: List[Hakutoive], answers: Answers, previousAnswers: Answers): Map[String, String] = {
    val previousHakutoiveetAnswers = previousAnswers.getOrElse(hakutoiveetPhase, Map())
    val hakutoiveetWithoutOldPreferences = previousHakutoiveetAnswers.filterKeys(s => !s.startsWith(HakutoiveetConverter.preferenceKeyPrefix))
    val updatedHakutoiveet = hakutoiveetWithoutOldPreferences ++ convertToAnswers(hakutoiveet, answers)
    updatedHakutoiveet
  }

  def answersContainHakutoive(answers: Map[String, String], hakutoive: Hakutoive) = {
    (hakutoive.get("Opetuspiste-id"), hakutoive.get(koulutusId)) match {
      case (Some(opetusPiste), Some(koulutus)) =>
        val flatAnswers = answers.toList.map {
          case (key, value) => (shortenKey(key), value)
        }
        flatAnswers.contains(("Opetuspiste-id", opetusPiste)) && flatAnswers.contains("Koulutus-id", koulutus)
      case _ => false
    }
  }

  def describe(hakutoive: Hakutoive) = {
    hakutoive.getOrElse("Opetuspiste", "") + " - " + hakutoive.getOrElse("Koulutus", "")
  }

  private def shortenKey(key: String): String = {
    key.substring(key.indexOf(getDelimiter(key)) + 1)
  }

  private def longKey(key: String, index: Int): String = {
    preferenceKeyPrefix + (index + 1) + getDelimiter(key) + key
  }

  private def getDelimiter(s: String) = if(s.contains("_")) "_" else "-"

  private def shortenNames(tuple: (String, Map[String, String])) = {
    def tupleWithShortKey(v: (String, String)) = v match {
      case (key: String, value: String) => (shortenKey(key), value)
    }
    tuple._2.map(tupleWithShortKey) ++ Map("priority" -> tuple._1)
  }

  private def groupPreferences(toiveet: Map[String, String]) = {
    val regex = "preference(\\d+).*".r

    toiveet.filter((key) => regex.pattern.matcher(key._1).matches()).groupBy((key) => key._1 match {
      case regex(x: String) => x
    })
  }

  private def convertEmptyPreferences(answers: Map[String, String]) = {
    if (answers.getOrElse("Koulutus-id", "").length() == 0) {
      Map("priority" -> answers.getOrElse("priority", ""))
    } else {
      answers
    }
  }
}
