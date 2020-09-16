package fi.vm.sade.hakemuseditori.tarjonta.vanha

import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Hakukohde, KohteenHakuaika, KoulutuksenAlkaminen}
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

  def parseHakukohde(json: JValue): Option[Hakukohde] = {
    for {
      obj <- (json \ "result").toOption
      oid = (obj \ "oid").extract[String]
      kaytetaanHakukohdekohtaistaHakuaikaa = (obj \ "kaytetaanHakukohdekohtaistaHakuaikaa").extractOrElse(false)
      hakuaikaId <- if (kaytetaanHakukohdekohtaistaHakuaikaa) { Some(None) } else { (obj \ "hakuaikaId").extractOpt[String].map(Some(_)) }
      ohjeetUudelleOpiskelijalle = (obj \ "ohjeetUudelleOpiskelijalle").extractOpt[String]
      hakuaika <- if (kaytetaanHakukohdekohtaistaHakuaikaa) { createHakuaika((obj \ "hakuaikaAlkuPvm").extractOpt[Long], (obj \ "hakuaikaLoppuPvm").extractOpt[Long]) } else { Some(None) }
      koulutuksenAlkaminen = createKoulutuksenAlkaminen((obj \ "koulutuksenAlkamisvuosi").extractOpt[Long], (obj \ "koulutuksenAlkamiskausiUri").extractOpt[String])
    } yield Hakukohde(oid, hakuaikaId, koulutuksenAlkaminen, hakuaika, ohjeetUudelleOpiskelijalle)
  }

  private def createHakuaika(hakuaikaAlkuPvm: Option[Long], hakuaikaLoppuPvm: Option[Long]) : Option[Option[List[KohteenHakuaika]]] = {
    (hakuaikaAlkuPvm, hakuaikaLoppuPvm) match {
      case (Some(a), l) => Some(Some(List(KohteenHakuaika(a, l))))
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
