package fi.vm.sade.omatsivut.domain

sealed trait QuestionNode {
  def title: String
}

case class QuestionGroup(title: String, questions: List[QuestionNode]) extends QuestionNode

trait Question extends QuestionNode {
  def help: String
  def questionType: String
  def id: QuestionId
  def context: QuestionContext
}

trait Optional extends Question {
  def options: List[Choice]
}

case class Text(context: QuestionContext, id: QuestionId, title: String, help: String, questionType: String = "Text") extends Question
case class TextArea(context: QuestionContext, id: QuestionId, title: String, help: String, questionType: String = "TextArea") extends Question
case class Radio(context: QuestionContext, id: QuestionId, title: String, help: String, options: List[Choice], questionType: String = "Radio") extends Optional
case class Checkbox(context: QuestionContext, id: QuestionId, title: String, help: String, options: List[Choice], questionType: String = "Checkbox") extends Optional
case class Dropdown(context: QuestionContext, id: QuestionId, title: String, help: String, options: List[Choice], questionType: String = "Dropdown") extends Optional

case class Choice(title: String, value: String, default: Boolean = false)

case class QuestionId(phaseId: String, questionId: String)

case class QuestionContext(path: List[String])