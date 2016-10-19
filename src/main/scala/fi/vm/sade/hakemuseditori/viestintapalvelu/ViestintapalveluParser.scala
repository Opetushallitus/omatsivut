package fi.vm.sade.hakemuseditori.viestintapalvelu

import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.tarjonta.domain.{KohteenHakuaika, Hakukohde, TarjontaHaku}
import fi.vm.sade.utils.slf4j.Logging
import org.json4s._

private object ViestintapalveluParser extends JsonFormats with Logging {

  def parseLetters(json: JValue): Option[List[Letter]] = {
    val res = for {
      obj <- (json \ "letters").toOption
      h <- obj.extractOpt[List[Letter]]
    } yield h
    if(!res.isDefined) { logger.warn("Letters is empty") }
    res
  }

  def parseHakukohde(json: JValue) = {
    for {
      obj <- (json \ "result").toOption
      oid = (obj \ "oid").extract[String]
      hakuaikaId = (obj \ "hakuaikaId").extractOpt[String]
      name = (obj \ "name").extractOpt[String].getOrElse("")
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
