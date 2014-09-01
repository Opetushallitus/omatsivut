package fi.vm.sade.omatsivut.hakemus.domain

import fi.vm.sade.haku.oppija.lomake.domain.elements.Notification.NotificationType

object QuestionNode {
  def flatten(qs: List[QuestionNode]): List[QuestionLeafNode] = {
    qs.flatMap(_.flatten)
  }
}

sealed trait QuestionNode {
  def title: String
  def flatten: List[QuestionLeafNode]
}

trait QuestionLeafNode extends QuestionNode {
  def id: QuestionId
  def flatten = List(this)
  def answerIds: List[AnswerId]
}

case class QuestionGroup(title: String, questions: List[QuestionNode]) extends QuestionNode {
  def flatten = questions.flatMap(_.flatten)
  def filter (f: (Question => Boolean)): QuestionGroup = {
    QuestionGroup(title, questions.flatMap {
      case q: TextNode => List(q)
      case q: Question => List(q).filter(f)
      case q: QuestionGroup => q.filter(f) match {
        case QuestionGroup(_, Nil) => Nil
        case q:QuestionGroup => List(q)
      }
      case _ => Nil
    })
  }
}

trait TextNode extends QuestionLeafNode {
  def answerIds: List[AnswerId] = Nil
}

trait Question extends QuestionLeafNode {
  def help: String
  def required: Boolean
  def questionType: String
  def answerIds: List[AnswerId] = List(AnswerId(id.phaseId, id.questionId))
}

trait WithOptions extends Question {
  def options: List[AnswerOption]
}

trait MultiValued extends WithOptions {
  override def answerIds = {
    val indices: List[Int] = options.zipWithIndex.map(_._2)
    indices.map { index =>
      AnswerId(id.phaseId, id.questionId + "-option_" + index)
    }
  }
}

case class Label(id: QuestionId, title: String) extends TextNode
case class Notification(id: QuestionId, title: String, notificationType: NotificationType) extends TextNode

case class Text(id: QuestionId, title: String, help: String, required: Boolean, maxlength: Int, questionType: String = "Text") extends Question
case class TextArea(id: QuestionId, title: String, help: String, required: Boolean, maxlength: Int, rows: Int, cols: Int, questionType: String = "TextArea") extends Question
case class Radio(id: QuestionId, title: String, help: String, options: List[AnswerOption], required: Boolean, questionType: String = "Radio") extends WithOptions
case class Checkbox(id: QuestionId, title: String, help: String, options: List[AnswerOption], required: Boolean, questionType: String = "Checkbox") extends MultiValued
case class Dropdown(id: QuestionId, title: String, help: String, options: List[AnswerOption], required: Boolean, questionType: String = "Dropdown") extends WithOptions

case class AnswerOption(title: String, value: String, default: Boolean = false)

case class QuestionId(phaseId: String, questionId: String)
case class AnswerId(phaseId: String, questionId: String)

case class QuestionContext(path: List[String])