package fi.vm.sade.hakemuseditori.json

import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, Hakuaika, KohteenHakuaika}
import fi.vm.sade.omatsivut.util.GenericJsonFormats
import org.json4s._

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
      Merge.merge(
        JObject(
          JField("id", JString(x.id)),
          JField("start", JInt(BigInt(x.start))),
          JField("active", JBool(x.active))),
        x.end.fold(JObject())(end => JObject(JField("end", JInt(BigInt(end))))))
  }
))

class KohteenHakuaikaSerializer extends CustomSerializer[KohteenHakuaika](format => (
  {
    case obj : JObject => obj.extract[KohteenHakuaika](GenericJsonFormats.genericFormats, Manifest.classType(classOf[KohteenHakuaika]))
  },
  {
    case x: KohteenHakuaika =>
      Merge.merge(
        JObject(
          JField("start", JInt(BigInt(x.start))),
          JField("active", JBool(x.active))),
        x.end.fold(JObject())(end => JObject(JField("end", JInt(BigInt(end))))))
  }
))
