package fi.vm.sade.omatsivut.domain

trait Question {
  def title: Translations
  def questionType: String
}

trait Optional extends Question {
  def options: List[Choice]
}

case class Text(title: Translations, questionType: String = "Text") extends Question
case class TextArea(title: Translations, questionType: String = "TextArea") extends Question
case class Radio(title: Translations, options: List[Choice], questionType: String = "Radio") extends Optional
case class Checkbox(title: Translations, options: List[Choice], questionType: String = "Checkbox") extends Optional
case class Dropdown(title: Translations, options: List[Choice], questionType: String = "Dropdown") extends Optional
case class Choice(title: Translations, default: Boolean)