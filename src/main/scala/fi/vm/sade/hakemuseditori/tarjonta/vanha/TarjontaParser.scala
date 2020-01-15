package fi.vm.sade.hakemuseditori.tarjonta.vanha

import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Hakukohde, KohteenHakuaika, KoulutuksenAlkaminen, TarjontaHaku}
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.JValue

object TarjontaParser extends JsonFormats with Logging {

  def parseHaku(json: JValue): Option[TarjontaHaku] = {
    val res = for {
      obj <- (json \ "result").toOption
      h <- obj.extractOpt[TarjontaHaku]
    } yield h
    if(!res.isDefined) { logger.warn("TarjontaHaku is empty") }
    res
  }

  def parseHakukohde(json: JValue) = {
    for {
      obj <- (json \ "result").toOption
      oid = (obj \ "oid").extract[String]
      hakuaikaId = (obj \ "hakuaikaId").extractOpt[String]
      ohjeetUudelleOpiskelijalle = (obj \ "ohjeetUudelleOpiskelijalle").extractOpt[String]
      name = (obj \ "name").extractOpt[String].getOrElse("")
      hakuaika = createHakuaika((obj \ "hakuaikaAlkuPvm").extractOpt[Long], (obj \ "hakuaikaLoppuPvm").extractOpt[Long])
      koulutuksenAlkaminen = createKoulutuksenAlkaminen((obj \ "koulutuksenAlkamisvuosi").extractOpt[Long], (obj \ "koulutuksenAlkamiskausiUri").extractOpt[String])
    } yield Hakukohde(oid, hakuaikaId, koulutuksenAlkaminen, hakuaika, ohjeetUudelleOpiskelijalle)
  }

  private def createHakuaika(hakuaikaAlkuPvm: Option[Long], hakuaikaLoppuPvm: Option[Long]) : Option[KohteenHakuaika] = {
    (hakuaikaAlkuPvm, hakuaikaLoppuPvm) match {
      case (Some(a), Some(l)) => Some(KohteenHakuaika(a, l))
      case _ => None
    }
  }

  private def createKoulutuksenAlkaminen(koulutuksenAlkamisvuosi: Option[Long], koulutuksenAlkamiskausiUri: Option[String]) : Option[KoulutuksenAlkaminen] = {
    (koulutuksenAlkamisvuosi, koulutuksenAlkamiskausiUri) match {
      case (Some(v), Some(k)) => Some(KoulutuksenAlkaminen(v, k))
      case _ => None
    }
  }
}
