package fi.vm.sade.omatsivut.json

import fi.vm.sade.omatsivut.domain._

import org.json4s._

object JsonFormats {
  val genericFormats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
}

trait JsonFormats {
  protected implicit val jsonFormats: Formats = JsonFormats.genericFormats ++ List(new QuestionNodeSerializer, new HakemusMuutosSerializer)
}

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

class HakemusMuutosSerializer extends Serializer[HakemusMuutos] {
  private val HakemusMuutosClass = classOf[HakemusMuutos]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), HakemusMuutos] = {
    case (TypeInfo(HakemusMuutosClass, _), JObject(fields: List[JField])) =>
      JObject(fields.map(stringyfiedAnswers)).extract[HakemusMuutos](JsonFormats.genericFormats, Manifest.classType(HakemusMuutosClass))
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case any: HakemusMuutos => Extraction.decompose(any)(JsonFormats.genericFormats)
  }

  private def stringyfiedAnswers(field: JField): JField  = field match {
    case ("answers", value) =>
      ("answers", value.map { field =>
        def toJString(v: Any) = JString(v.toString)
        def rec(v: JValue): JValue = v match {
          case JInt(i) => toJString(i)
          case JBool(b) => toJString(b)
          case JDecimal(dec) => toJString(dec)
          case JDouble(d) => toJString(d)
          case x => x
        }
        rec(field)
      })
    case field => field
  }
}