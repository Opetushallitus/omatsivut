package fi.vm.sade.omatsivut.domain

trait Question {
  def title: Translations
  def questionType: String
  def id: QuestionId
}

trait Optional extends Question {
  def options: List[Choice]
}

case class Text(id: QuestionId, title: Translations, questionType: String = "Text") extends Question
case class TextArea(id: QuestionId, title: Translations, questionType: String = "TextArea") extends Question
case class Radio(id: QuestionId, title: Translations, options: List[Choice], questionType: String = "Radio") extends Optional
case class Checkbox(id: QuestionId, title: Translations, options: List[Choice], questionType: String = "Checkbox") extends Optional
case class Dropdown(id: QuestionId, title: Translations, options: List[Choice], questionType: String = "Dropdown") extends Optional

case class Choice(title: Translations, default: Boolean)

case class QuestionId(phaseId: String, questionId: String)