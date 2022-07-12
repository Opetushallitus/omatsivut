package fi.vm.sade.hakemuseditori.tarjonta.vanha

import fi.vm.sade.hakemuseditori.domain.Language
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

  def parseHakukohde(json: JValue, lang: Language.Language): Option[Hakukohde] = {
    for {
      obj <- (json \ "result").toOption
      oid = (obj \ "oid").extract[String]
      nimiMap = (obj \ "nimi").extractOpt[Map[String, String]].getOrElse(Map())
      nimi = nimiMap.get("kieli_" + lang.toString).orElse(nimiMap.get("kieli_fi")).getOrElse("?")
      kaytetaanHakukohdekohtaistaHakuaikaa = (obj \ "kaytetaanHakukohdekohtaistaHakuaikaa").extractOrElse(false)
      hakuaikaId <- if (kaytetaanHakukohdekohtaistaHakuaikaa) { Some(None) } else { (obj \ "hakuaikaId").extractOpt[String].map(Some(_)) }
      ohjeetUudelleOpiskelijalle = (obj \ "ohjeetUudelleOpiskelijalle").extractOpt[String]
      hakuaika <- if (kaytetaanHakukohdekohtaistaHakuaikaa) { createHakuaika((obj \ "hakuaikaAlkuPvm").extractOpt[Long], (obj \ "hakuaikaLoppuPvm").extractOpt[Long]) } else { Some(None) }
      koulutuksenAlkaminen = createKoulutuksenAlkaminen((obj \ "koulutuksenAlkamisvuosi").extractOpt[Long], (obj \ "koulutuksenAlkamiskausiUri").extractOpt[String])
      yhdenPaikanSaanto = (obj \ "yhdenPaikanSaanto" \ "voimassa").extractOrElse(false)
    } yield Hakukohde(oid, nimi, hakuaikaId, koulutuksenAlkaminen, hakuaika, ohjeetUudelleOpiskelijalle, yhdenPaikanSaanto)
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
