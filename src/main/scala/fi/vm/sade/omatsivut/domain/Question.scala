package fi.vm.sade.omatsivut.domain

sealed trait QuestionNode {
  def title: String
  def flatten: List[Question]
}

case class QuestionGroup(title: String, questions: List[QuestionNode]) extends QuestionNode {
  def flatten = questions.flatMap(_.flatten)
}

trait Question extends QuestionNode {
  def help: String
  def questionType: String
  def id: QuestionId
  def flatten = List(this)
}

trait Optional extends Question {
  def options: List[Choice]
}

case class Text(id: QuestionId, title: String, help: String, questionType: String = "Text") extends Question
case class TextArea(id: QuestionId, title: String, help: String, questionType: String = "TextArea") extends Question
case class Radio(id: QuestionId, title: String, help: String, options: List[Choice], questionType: String = "Radio") extends Optional
case class Checkbox(id: QuestionId, title: String, help: String, options: List[Choice], questionType: String = "Checkbox") extends Optional
case class Dropdown(id: QuestionId, title: String, help: String, options: List[Choice], questionType: String = "Dropdown") extends Optional

case class Choice(title: String, value: String, default: Boolean = false)

case class QuestionId(phaseId: String, questionId: String)

case class QuestionContext(path: List[String])