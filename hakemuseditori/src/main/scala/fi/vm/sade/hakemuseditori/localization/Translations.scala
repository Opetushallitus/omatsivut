package fi.vm.sade.hakemuseditori.localization

import fi.vm.sade.hakemuseditori.domain.Language._
import org.json4s._

trait Translations {
  def getTranslations(implicit lang: Language): JValue
  def getTranslation(path: String*)(implicit lang: Language): String
}

trait TranslationsComponent {
  def translations: Translations
}
