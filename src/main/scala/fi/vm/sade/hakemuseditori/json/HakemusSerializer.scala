package fi.vm.sade.hakemuseditori.json

import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus
import fi.vm.sade.omatsivut.util.GenericJsonFormats
import org.json4s._

class HakemusSerializer extends Serializer[Hakemus] {
  private val HakemusClass = classOf[Hakemus]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Hakemus] = {
    case (TypeInfo(HakemusClass, _), JObject(fields: List[JField])) =>
      JObject(fields.map(AnswerSerializer.stringyfiedAnswers)).extract[Hakemus](GenericJsonFormats.genericFormats, Manifest.classType(HakemusClass))
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case any: Hakemus => Extraction.decompose(any)(GenericJsonFormats.genericFormats)
  }
}
