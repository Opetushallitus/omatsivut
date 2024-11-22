package fi.vm.sade.hakemuseditori.lomake.domain


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

  def filter(f: (Question => Boolean)): QuestionGroup = {
    QuestionGroup(title, questions.flatMap { case q: TextNode => List(q)
    case q: Question => List(q).filter(f)
    case q: QuestionGroup => q.filter(f) match {
      case QuestionGroup(_, Nil) => Nil
      case QuestionGroup(title, list) => {
        removeExtraLabels(list) match {
          case Nil => Nil
          case some => List(QuestionGroup(title, some))
        }
      }
    }
    case _ => Nil
    })
  }

  private def removeExtraLabels(questions: List[QuestionNode]): List[QuestionNode] = {
    questions match {
      case Nil => Nil
      case List(textNode: TextNode) => Nil
      case List(node) => questions
      case node1 :: node2 :: tail if node1.isInstanceOf[TextNode] && node2.isInstanceOf[TextNode] => removeExtraLabels(node2 :: tail)
      case node1 :: node2 :: tail if node2.isInstanceOf[TextNode] => node1 :: removeExtraLabels(node2 :: tail)
      case node1 :: node2 :: tail => node1 :: node2 :: removeExtraLabels(tail)
    }
  }
}

trait TextNode extends QuestionLeafNode {
  def answerIds: List[AnswerId] = Nil
}

trait Question extends QuestionLeafNode {
  def help: String

  def verboseHelp: String

  def required: Boolean

  def questionType: String

  def answerIds: List[AnswerId] = List(AnswerId(id.phaseId, id.questionId))
}

trait WithOptions extends Question {
  def options: List[AnswerOption]
}

trait MultiValued extends WithOptions {
  override def answerIds = {
    options.map { option =>
      AnswerId(id.phaseId, option.value)
    }
  }
}

case class Label(id: QuestionId, title: String) extends TextNode

case class Text(id: QuestionId, title: String, help: String, verboseHelp: String, required: Boolean, maxlength: Int, questionType: String = "Text") extends Question

case class TextArea(id: QuestionId, title: String, help: String, verboseHelp: String, required: Boolean, maxlength: Int, rows: Int, cols: Int, questionType: String = "TextArea") extends Question

case class Radio(id: QuestionId, title: String, help: String, verboseHelp: String, options: List[AnswerOption], required: Boolean, questionType: String = "Radio") extends WithOptions

case class Checkbox(id: QuestionId, title: String, help: String, verboseHelp: String, options: List[AnswerOption], required: Boolean, questionType: String = "Checkbox") extends MultiValued

case class GradeAverageCheckbox(id: QuestionId, title: String, help: String, verboseHelp: String, required: Boolean, options: List[AnswerOption], questionType: String = "Checkbox") extends WithOptions

case class Dropdown(id: QuestionId, title: String, help: String, verboseHelp: String, options: List[AnswerOption], required: Boolean, questionType: String = "Dropdown") extends WithOptions

case class AnswerOption(title: String, value: String, default: Boolean = false)

case class QuestionId(phaseId: String, questionId: String)

case class AnswerId(phaseId: String, questionId: String)

case class QuestionContext(path: List[String])
