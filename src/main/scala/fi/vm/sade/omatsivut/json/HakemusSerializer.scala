package fi.vm.sade.omatsivut.json

import fi.vm.sade.omatsivut.hakemus.domain.Hakemus
import org.json4s._

class HakemusSerializer extends Serializer[Hakemus] {
  private val HakemusClass = classOf[Hakemus]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Hakemus] = {
    case (TypeInfo(HakemusClass, _), JObject(fields: List[JField])) =>
      JObject(fields.map(stringyfiedAnswers)).extract[Hakemus](JsonFormats.genericFormats, Manifest.classType(HakemusClass))
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case any: Hakemus => Extraction.decompose(any)(JsonFormats.genericFormats)
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
