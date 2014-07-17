package fi.vm.sade.omatsivut.domain

sealed trait QuestionNode {
  def title: Translations
}

case class QuestionGroup(title: Translations, questions: List[QuestionNode]) extends QuestionNode

trait Question extends QuestionNode {
  def help: Translations
  def questionType: String
  def id: QuestionId
  def context: QuestionContext
}

trait Optional extends Question {
  def options: List[Choice]
}

case class Text(context: QuestionContext, id: QuestionId, title: Translations, help: Translations, questionType: String = "Text") extends Question
case class TextArea(context: QuestionContext, id: QuestionId, title: Translations, help: Translations, questionType: String = "TextArea") extends Question
case class Radio(context: QuestionContext, id: QuestionId, title: Translations, help: Translations, options: List[Choice], questionType: String = "Radio") extends Optional
case class Checkbox(context: QuestionContext, id: QuestionId, title: Translations, help: Translations, options: List[Choice], questionType: String = "Checkbox") extends Optional
case class Dropdown(context: QuestionContext, id: QuestionId, title: Translations, help: Translations, options: List[Choice], questionType: String = "Dropdown") extends Optional

case class Choice(title: Translations, value: String, default: Boolean = false)

case class QuestionId(phaseId: String, questionId: String)

case class QuestionContext(path: List[String])