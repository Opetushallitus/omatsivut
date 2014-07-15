package fi.vm.sade.omatsivut.hakemus

/**
 * Created by singen on 15.7.2014.
 */
object HakutoiveetConverter {
  def convert(toiveet: Map[String, String]): List[Map[String, String]] = {
    groupPreferences(toiveet)
      .toList
      .map(shortenNames)
      .map(convertEmptyPreferences)
      .sortBy(map => map.get("priority"))
      .map((m) => m.filterKeys { Set("priority").contains(_) == false})
  }

  private def shortenKey(v: (String, String), delimiter: Char = '-') = {
    v._1.substring(v._1.indexOf(delimiter) + 1)
  }

  private def tupleWithShortKey(v: (String, String)) = {
    if (v._1.contains("_")) (shortenKey(v, '_'), v._2) else (shortenKey(v), v._2)
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

  private def shortenNames(tuple: (String, Map[String, String])) = {
    tuple._2.map(tupleWithShortKey) ++ Map("priority" -> tuple._1)
  }
}
