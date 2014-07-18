package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.domain.Hakemus
import fi.vm.sade.omatsivut.domain.Hakemus._

/**
 * Created by singen on 15.7.2014.
 */
object HakutoiveetConverter {
  val preferenceKeyPrefix: String = "preference"
  val hakutoiveetPhase: String = "hakutoiveet"

  def convertFromAnswers(answers: Map[String, String]): List[Hakutoive] = {
    groupPreferences(answers)
      .toList
      .map(shortenNames)
      .map(convertEmptyPreferences)
      .sortBy(map => map.get("priority"))
      .map((m) => m.filterKeys { Set("priority").contains(_) == false})
  }

  def convertToAnswers(hakutoiveet: List[Hakutoive]): Map[String, String] = {
    hakutoiveet.zipWithIndex.flatMap {
      case (hakutoive, index) => hakutoive.map {
        case (key, value) => (preferenceKeyPrefix + (index + 1) + getDelimiter(key) + key, value)
      }
    }.toMap[String, String]
  }

  def updateAnswers(hakemus: Hakemus, answers: Map[String, String]): Map[String, String] = {
    val hakuToiveetWithEmptyValues = answers.filterKeys(s => s.startsWith(HakutoiveetConverter.preferenceKeyPrefix)).mapValues(s => "")
    val hakutoiveetWithoutOldPreferences = answers.filterKeys(s => !s.startsWith(HakutoiveetConverter.preferenceKeyPrefix))
    val hakutoiveetAnswers: Map[String, String] = hakemus.answers.getOrElse(hakutoiveetPhase, Map())
    val updatedHakutoiveet = hakutoiveetWithoutOldPreferences ++ hakuToiveetWithEmptyValues ++ HakutoiveetConverter.convertToAnswers(hakemus.hakutoiveet) ++ hakutoiveetAnswers
    updatedHakutoiveet
  }

  def answersContainHakutoive(answers: Map[String, String], hakutoive: Hakutoive) = {
    (hakutoive.get("Opetuspiste-id"), hakutoive.get("Koulutus-id")) match {
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
