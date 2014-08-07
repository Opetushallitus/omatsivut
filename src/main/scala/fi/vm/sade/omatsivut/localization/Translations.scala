package fi.vm.sade.omatsivut.localization
import fi.vm.sade.omatsivut.domain.Language.Language
import fi.vm.sade.omatsivut.json.JsonFormats
import org.json4s._
import org.json4s.jackson.JsonMethods._

object Translations extends JsonFormats {
  def getTranslations(implicit lang: Language) = {
    val fileName: String = "/translations/" + lang.toString + ".json"
    val text = io.Source.fromInputStream(getClass.getResourceAsStream(fileName)).mkString
    parse(text)
  }
}
