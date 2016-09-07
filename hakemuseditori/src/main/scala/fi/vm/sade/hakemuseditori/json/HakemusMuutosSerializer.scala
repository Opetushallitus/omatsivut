package fi.vm.sade.hakemuseditori.json

import fi.vm.sade.hakemuseditori.hakemus.domain.HakemusMuutos
import fi.vm.sade.utils.json4s.GenericJsonFormats
import org.json4s._

class HakemusMuutosSerializer extends Serializer[HakemusMuutos] {
  private val HakemusMuutosClass = classOf[HakemusMuutos]

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), HakemusMuutos] = {
    case (TypeInfo(HakemusMuutosClass, _), JObject(fields: List[JField])) =>
      JObject(fields.map(AnswerSerializer.stringyfiedAnswers)).extract[HakemusMuutos](GenericJsonFormats.genericFormats, Manifest.classType(HakemusMuutosClass))
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case any: HakemusMuutos => Extraction.decompose(any)(GenericJsonFormats.genericFormats)
  }
}
