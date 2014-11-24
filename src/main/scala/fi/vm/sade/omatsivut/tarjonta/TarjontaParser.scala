package fi.vm.sade.omatsivut.tarjonta

import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.tarjonta.domain.{TarjontaHaku, KohteenHakuaika, Hakukohde}
import org.json4s.JValue

private object TarjontaParser extends JsonFormats {

  def parseHaku(json: JValue) = {
    for {
      obj <- (json \ "result").toOption
      h <- obj.extractOpt[TarjontaHaku]
    } yield h
  }

  def parseHakukohde(json: JValue) = {
    for {
      obj <- (json \ "result").toOption
      oid = (obj \ "oid").extract[String]
      hakuaikaId = (obj \ "hakuaikaId").extractOpt[String]
      hakuaika = createHakuaika((obj \ "hakuaikaAlkuPvm").extractOpt[Long], (obj \ "hakuaikaLoppuPvm").extractOpt[Long])
    } yield Hakukohde(oid, hakuaikaId, hakuaika)
  }

  private def createHakuaika(hakuaikaAlkuPvm: Option[Long], hakuaikaLoppuPvm: Option[Long]) : Option[KohteenHakuaika] = {
    (hakuaikaAlkuPvm, hakuaikaLoppuPvm) match {
      case (Some(a), Some(l)) => Some(KohteenHakuaika(a, l))
      case _ => None
    }
  }
}
