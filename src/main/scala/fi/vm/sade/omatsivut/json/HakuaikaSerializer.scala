package fi.vm.sade.omatsivut.json

import fi.vm.sade.omatsivut.tarjonta.{KohteenHakuaika, Hakuaika}
import org.json4s._

class HakuaikaSerializer extends CustomSerializer[Hakuaika](format => (
  {
    case _ =>
      throw new NotImplementedError()
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
    case _ =>
      throw new NotImplementedError()
  },
  {
    case x: KohteenHakuaika =>
      JObject(
          JField("start", JInt(BigInt(x.start))) ::
          JField("end", JInt(BigInt(x.end))) ::
          JField("active", JBool(x.active)) :: Nil)
  }
  ))
