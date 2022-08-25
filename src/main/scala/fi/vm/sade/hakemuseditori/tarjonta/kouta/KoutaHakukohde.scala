package fi.vm.sade.hakemuseditori.tarjonta.kouta

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Hakukohde, KohteenHakuaika, KoulutuksenAlkaminen}
import fi.vm.sade.hakemuseditori.tarjonta.vanha.YhdenPaikanSaanto
import fi.vm.sade.utils.slf4j.Logging

import scala.util.{Failure, Success, Try}

case class PaateltyAlkamiskausi(kausiUri: String,
                                 vuosi: String)

sealed case class KoutaHakukohde(alkamiskausiKoodiUri: Option[String],
                                 alkamisvuosi: Option[String],
                                 kaytetaanHaunAikataulua: Option[Boolean],
                                 kaytetaanHaunAlkamiskautta: Option[Boolean],
                                 hakuajat: List[KoutaHakuaika],
                                 oid: String,
                                 nimi: Map[String, String],
                                 yhdenPaikanSaanto: YhdenPaikanSaanto,
                                 uudenOpiskelijanUrl: Map[String, String],
                                 paateltyAlkamiskausi: Option[PaateltyAlkamiskausi]) {

  def getLocalizedUudenOpiskelijanUrl(lang: Language): Option[String] = {
    val desiredLanguage = List(lang.toString, "fi", "sv", "en") find { k => uudenOpiskelijanUrl.get(k).exists(_.nonEmpty) }
    desiredLanguage flatMap { s => uudenOpiskelijanUrl.get(s) }
  }

  def getLocalizedName(lang: Language): String = {
    val desiredLanguage = List(lang.toString, "fi", "sv", "en") find { k => nimi.get(k).exists(_.nonEmpty) }
    desiredLanguage flatMap { s => nimi.get(s) } getOrElse("?")
  }
}

object KoutaHakukohde extends Logging {
  def toHakukohde(koutaHakukohde: KoutaHakukohde, lang: Language.Language): Try[Hakukohde] = {
    for {
      kohteenHakuaika <- extractKohteenHakuajat(koutaHakukohde)
      koulutuksenAlkaminen <- Try { getKoulutuksenAlkaminen(koutaHakukohde) }
    } yield Hakukohde(hakuaikaId = Some("kouta-hakuaika-id"),
      koulutuksenAlkaminen = koulutuksenAlkaminen,
      hakukohdekohtaisetHakuajat = kohteenHakuaika,
      ohjeetUudelleOpiskelijalle = koutaHakukohde.getLocalizedUudenOpiskelijanUrl(lang),
      oid = koutaHakukohde.oid,
      nimi = koutaHakukohde.getLocalizedName(lang),
      yhdenPaikanSaanto = koutaHakukohde.yhdenPaikanSaanto.voimassa)
  }

  private def extractKohteenHakuajat(koutaHakukohde: KoutaHakukohde) : Try[Option[List[KohteenHakuaika]]] = {
    if (koutaHakukohde.kaytetaanHaunAikataulua.getOrElse(false))
      Success(None)
    else
      koutaHakukohde.hakuajat.foldRight[Try[Option[List[KohteenHakuaika]]]](Success(Some(Nil))) {
        case (koutaHakuaika, Success(Some(hs))) => toKohteenHakuaika(koutaHakuaika).map(aika => Some(aika :: hs))
        case (_, failure) => failure
      }
  }

  private def toKohteenHakuaika(koutaHakuaika: KoutaHakuaika): Try[KohteenHakuaika] = {
    koutaHakuaika.toKohteenHakuaika
      .recoverWith {
        case exception: Throwable => Failure(new RuntimeException("Failed to form kohteenHakuaika", exception))
      }
  }

  private def getKoulutuksenAlkaminen(koutaHakukohde: KoutaHakukohde): Option[KoulutuksenAlkaminen] = {
    try {
      val vuosi = koutaHakukohde.paateltyAlkamiskausi.map(ak => ak.vuosi.toLong)
      val kausi = koutaHakukohde.paateltyAlkamiskausi.map(ak => ak.kausiUri)
      (vuosi, kausi) match {
        case (Some(v), Some(k)) => Some(KoulutuksenAlkaminen(v, k))
        case _ =>
          logger.warn(s"Ei alkamiskautta tiedossa koutaHakukohteelle ${koutaHakukohde.oid}")
          None
      }
    } catch {
      case e: Exception =>
        logger.error(s"Virhe pääteltäessä alkamiskautta koutaHakukohteelle ${koutaHakukohde.oid}: ${e.getMessage}", e)
        None
    }
  }
}
