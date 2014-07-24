package fi.vm.sade.omatsivut.domain

case class Translations(translations: Map[String, String])

object Translations {
  def apply(text: String) = {
    new Translations(Map("fi" -> text)) // TODO: kieliversiot
  }
}
