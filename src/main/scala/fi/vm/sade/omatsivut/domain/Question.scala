package fi.vm.sade.omatsivut.domain

trait Question {
  def title: Translations
}

trait Optional extends Question {
  def options: List[Translations]
}

case class Text(title: Translations) extends Question
case class TextArea(title: Translations) extends Question
case class Radio(title: Translations, options: List[Translations]) extends Optional
case class Checkbox(title: Translations, options: List[Translations]) extends Optional
case class Dropdown(title: Translations, options: List[Translations]) extends Optional
