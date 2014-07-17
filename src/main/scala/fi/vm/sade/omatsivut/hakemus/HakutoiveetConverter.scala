package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.domain.Hakemus

/**
 * Created by singen on 15.7.2014.
 */
object HakutoiveetConverter {
  val preferenceKeyPrefix: String = "preference"
  val hakutoiveetPhase: String = "hakutoiveet"

  def convertFromAnswers(toiveet: Map[String, String]): List[Map[String, String]] = {
    groupPreferences(toiveet)
      .toList
      .map(shortenNames)
      .map(convertEmptyPreferences)
      .sortBy(map => map.get("priority"))
      .map((m) => m.filterKeys { Set("priority").contains(_) == false})
  }

  def convertToAnswers(hakutoiveet: List[Hakemus.Hakutoive]): Map[String, String] = {
    def getDelimiter(s: String) = if(s.contains("_")) "_" else "-"

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

  private def shortenNames(tuple: (String, Map[String, String])) = {
    def tupleWithShortKey(v: (String, String)) = v match {
      case (key: String, value: String) =>
        if (key.contains("_"))
          (shortenKey(key, '_'), value)
        else
          (shortenKey(key, '-'), value)
    }
    def shortenKey(v: String, delimiter: Char): String = {
      v.substring(v.indexOf(delimiter) + 1)
    }
    tuple._2.map(tupleWithShortKey) ++ Map("priority" -> tuple._1)
  }

  private def groupPreferences(toiveet: Map[String, String]) = {
    val regex = "preference(\\d+).*".r

    toiveet.filter((key) => regex.pattern.matcher(key._1).matches()).groupBy((key) => key._1 match {
      case regex(x: String) => x
    })
  }

  private def convertEmptyPreferences(toiveet: Map[String, String]) = {
    if (toiveet.getOrElse("Koulutus-id", "").length() == 0) {
      Map("priority" -> toiveet.getOrElse("priority", ""))
    } else {
      toiveet
    }
  }
}
