package fi.vm.sade.omatsivut.domain

trait Question {
  def title: Translations
  def questionType: String
  def id: QuestionId
  def context: QuestionContext
}

trait Optional extends Question {
  def options: List[Choice]
}

case class Text(context: QuestionContext, id: QuestionId, title: Translations, questionType: String = "Text") extends Question
case class TextArea(context: QuestionContext, id: QuestionId, title: Translations, questionType: String = "TextArea") extends Question
case class Radio(context: QuestionContext, id: QuestionId, title: Translations, options: List[Choice], questionType: String = "Radio") extends Optional
case class Checkbox(context: QuestionContext, id: QuestionId, title: Translations, options: List[Choice], questionType: String = "Checkbox") extends Optional
case class Dropdown(context: QuestionContext, id: QuestionId, title: Translations, options: List[Choice], questionType: String = "Dropdown") extends Optional

case class Choice(title: Translations, value: String, default: Boolean = false)

case class QuestionId(phaseId: String, questionId: String)

case class QuestionContext(path: List[String])