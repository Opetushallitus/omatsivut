package fi.vm.sade.omatsivut.domain

sealed trait QuestionNode {
  def title: String
  def flatten: List[Question]
}

case class QuestionGroup(title: String, questions: List[QuestionNode]) extends QuestionNode {
  def flatten = questions.flatMap(_.flatten)
  def filter (f: (Question => Boolean)): QuestionGroup = {
    QuestionGroup(title, questions.flatMap {
      case q: Question => List(q).filter(f)
      case q: QuestionGroup => q.filter(f) match {
        case QuestionGroup(_, Nil) => Nil
        case q:QuestionGroup => List(q)
      }
    })
  }
}

trait Question extends QuestionNode {
  def help: String
  def required: Boolean
  def questionType: String
  def id: QuestionId
  def flatten = List(this)
}

trait Optional extends Question {
  def options: List[Choice]
}

case class Text(id: QuestionId, title: String, help: String, required: Boolean, maxlength: Int, questionType: String = "Text") extends Question
case class TextArea(id: QuestionId, title: String, help: String, required: Boolean, maxlength: Int, rows: Int, cols: Int, questionType: String = "TextArea") extends Question
case class Radio(id: QuestionId, title: String, help: String, options: List[Choice], required: Boolean, questionType: String = "Radio") extends Optional
case class Checkbox(id: QuestionId, title: String, help: String, options: List[Choice], required: Boolean, questionType: String = "Checkbox") extends Optional
case class Dropdown(id: QuestionId, title: String, help: String, options: List[Choice], required: Boolean, questionType: String = "Dropdown") extends Optional

case class Choice(title: String, value: String, default: Boolean = false)

case class QuestionId(phaseId: String, questionId: String)

case class QuestionContext(path: List[String])