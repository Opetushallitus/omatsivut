package fi.vm.sade.omatsivut.localization

import fi.vm.sade.omatsivut.domain.Language
import Language.Language
import fi.vm.sade.omatsivut.json.JsonFormats
import org.json4s._
import org.json4s.jackson.JsonMethods._

object Translations extends JsonFormats {
  lazy val translations = Map(
    "fi" -> loadTranslation("fi"),
    "en" -> loadTranslation("en"),
    "sv" -> loadTranslation("sv")
  )

  private def loadTranslation(lang: String) = {
    val fileName: String = "/translations/" + lang + ".json"
    val text = io.Source.fromInputStream(getClass.getResourceAsStream(fileName)).mkString
    parse(text)
  }

  def getTranslations(implicit lang: Language) = translations(lang.toString)

  def getTranslation(path: String*)(implicit lang: Language) = {
    path.foldLeft(getTranslations){ case (json, pathElem) => json \ pathElem }.extract[String]
  }
}
