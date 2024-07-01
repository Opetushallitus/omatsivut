package fi.vm.sade.hakemuseditori.hakemus

import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus._

object HakutoiveetConverter {
  val hakutoiveetPhase: String = "hakutoiveet" // OppijaConstants.PHASE_APPLICATION_OPTIONS
  val opetuspisteId: String = "Opetuspiste-id"

  def convertFromAnswers(answers: Answers): List[HakutoiveData] = {
    groupPreferences(answers.getOrElse(hakutoiveetPhase , Map()))
      .toList
      .map(shortenNames)
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

}
