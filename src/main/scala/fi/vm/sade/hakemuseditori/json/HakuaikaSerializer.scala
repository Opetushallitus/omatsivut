package fi.vm.sade.hakemuseditori.json

import fi.vm.sade.hakemuseditori.tarjonta.domain.{KohteenHakuaika, Hakuaika, Haku}
import fi.vm.sade.utils.json4s.GenericJsonFormats
import org.json4s._

object JsonProcessor {
  def serializeWithField[T](obj: T, field: JField) = {
    val newField = JObject(field :: Nil)
    implicit val formats: Formats = DefaultFormats ++ List(new HakuaikaSerializer)
    Extraction.decompose(obj) merge newField
  }
}

class HakuSerializer extends CustomSerializer[Haku](format => (
  {
    case obj : JObject =>
      obj.extract[Haku](GenericJsonFormats.genericFormats, Manifest.classType(classOf[Haku]))
  },
  {
    case x: Haku =>
      Extraction.decompose(x)(DefaultFormats ++ List(new HakuaikaSerializer))
  }
))

class HakuaikaSerializer extends CustomSerializer[Hakuaika](format => (
  {
    case obj : JObject => obj.extract[Hakuaika](GenericJsonFormats.genericFormats, Manifest.classType(classOf[Hakuaika]))
  },
  {
    case x: Hakuaika =>
      JObject(
        JField("id", JString(x.id)) ::
          JField("start", JInt(BigInt(x.start))) ::
          JField("end", JInt(BigInt(x.end))) ::
          JField("active", JBool(x.active)) :: Nil)
  }
))

class KohteenHakuaikaSerializer extends CustomSerializer[KohteenHakuaika](format => (
  {
    case obj : JObject => obj.extract[KohteenHakuaika](GenericJsonFormats.genericFormats, Manifest.classType(classOf[KohteenHakuaika]))
  },
  {
    case x: KohteenHakuaika =>
      JObject(
        JField("start", JInt(BigInt(x.start))),
        JField("end", JInt(BigInt(x.end))),
        JField("active", JBool(x.active)))
  }
))
