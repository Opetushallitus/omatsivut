package fi.vm.sade.omatsivut.json

import fi.vm.sade.omatsivut.lomake.domain._
import org.json4s._

class QuestionNodeSerializer extends Serializer[QuestionNode] {
  private val QuestionNodeClass = classOf[QuestionNode]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), QuestionNode] = {
    case (TypeInfo(QuestionNodeClass, _), json) =>
      json match {
      case questionGroup: JObject if questionGroup.values.contains("questions") =>
        json.extract[QuestionGroup]
      case question: JObject => question.values.get("questionType") match {
        case Some("Text") => json.extract[Text]
        case Some("TextArea") => json.extract[TextArea]
        case Some("Radio") => json.extract[Radio]
        case Some("Checkbox") => json.extract[Checkbox]
        case Some("Dropdown") => json.extract[Dropdown]
        case unknown => throw new MappingException("Unknown question type " + unknown)
      }

      case x => throw new MappingException("Can't convert " + x + " to QuestionNode")
    }
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case any: QuestionNode => Extraction.decompose(any)(JsonFormats.genericFormats)
  }
}
