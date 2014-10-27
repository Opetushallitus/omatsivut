package fi.vm.sade.omatsivut.json

import fi.vm.sade.omatsivut.tarjonta.{Haku, KohteenHakuaika, Hakuaika}
import org.json4s._

// TOOD Refactor
class HakuSerializer extends CustomSerializer[Haku](format => (
  {
    case obj : JObject => obj.extract[Haku](JsonFormats.genericFormats, Manifest.classType(classOf[Haku]))
  },
  {
    case x: Haku =>
      val obj = JObject(JField("active", JBool(x.active)) :: Nil)
      implicit val formats: Formats = DefaultFormats ++ List(new HakuaikaSerializer)
      Extraction.decompose(x) merge obj
  }
))

class HakuaikaSerializer extends CustomSerializer[Hakuaika](format => (
  {
    case obj : JObject => obj.extract[Hakuaika](JsonFormats.genericFormats, Manifest.classType(classOf[Hakuaika]))
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
    case obj : JObject => obj.extract[KohteenHakuaika](JsonFormats.genericFormats, Manifest.classType(classOf[KohteenHakuaika]))
  },
  {
    case x: KohteenHakuaika =>
      JObject(
          JField("start", JInt(BigInt(x.start))) ::
          JField("end", JInt(BigInt(x.end))) ::
          JField("active", JBool(x.active)) :: Nil)
  }
))
