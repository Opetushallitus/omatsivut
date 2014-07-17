package fi.vm.sade.omatsivut.json

import fi.vm.sade.omatsivut.domain._
import org.json4s._

object JsonFormats {
  val genericFormats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
}

trait JsonFormats {
  protected implicit val jsonFormats: Formats = JsonFormats.genericFormats ++ List(new QuestionNodeSerializer)
}

class QuestionNodeSerializer extends Serializer[QuestionNode] {
  private val QuestionNodeClass = classOf[QuestionNode]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), QuestionNode] = {
    case (TypeInfo(QuestionNodeClass, _), json) =>
      json match {
      case questionGroup: JObject if questionGroup.values.contains("questions") =>
        json.extract[QuestionGroup]
      case question: JObject if question.values.get("questionType") == Some("Text") =>
        json.extract[Text]
      case question: JObject if question.values.get("questionType") == Some("TextArea") =>
        json.extract[TextArea]
      case question: JObject if question.values.get("questionType") == Some("Radio") =>
        json.extract[Radio]
      case question: JObject if question.values.get("questionType") == Some("Checkbox") =>
        json.extract[Checkbox]
      case question: JObject if question.values.get("questionType") == Some("Dropdown") =>
        json.extract[Dropdown]
      case x => throw new MappingException("Can't convert " + x + " to QuestionNode")
    }
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case any: QuestionNode =>
      println(any)
      val result = Extraction.decompose(any)(JsonFormats.genericFormats)
      println(result)
      result
  }
}
