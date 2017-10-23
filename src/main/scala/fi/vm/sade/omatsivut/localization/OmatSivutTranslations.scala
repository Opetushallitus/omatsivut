package fi.vm.sade.omatsivut.localization

import fi.vm.sade.hakemuseditori.domain.Language
import Language.Language
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.localization.Translations
import org.json4s._
import org.json4s.jackson.JsonMethods._

object OmatSivutTranslations extends Translations with JsonFormats {
  lazy val translations = Map(
    "fi" -> loadTranslation("fi"),
    "en" -> loadTranslation("en"),
    "sv" -> loadTranslation("sv")
  )

  private def loadTranslation(lang: String) = {
    val fileName: String = "/translations/" + lang + ".json"
    val text = io.Source.fromInputStream(getClass.getResourceAsStream(fileName)).mkString
    parse(text, useBigDecimalForDouble = false)
  }

  def getTranslations(implicit lang: Language): JValue = translations(lang.toString)

  def getTranslation(path: String*)(implicit lang: Language): String = {
    path.foldLeft(getTranslations){ case (json, pathElem) => json \ pathElem }.extract[String]
  }
}
