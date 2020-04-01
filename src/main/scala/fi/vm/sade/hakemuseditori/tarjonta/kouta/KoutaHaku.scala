package fi.vm.sade.hakemuseditori.tarjonta.kouta

import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.ohjausparametrit.domain.HaunAikataulu
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, Hakuaika}
import fi.vm.sade.hakemuseditori.tarjonta.domain.HakuTyyppi.{Erillishaku, JatkuvaHaku, Yhteishaku}

import scala.util.{Failure, Try}

sealed case class KoutaHaku(hakuajat: List[KoutaHakuaika],
                            hakutapaKoodiUri: Option[String],
                            kohdejoukkoKoodiUri: String,
                            kohdejoukonTarkenneKoodiUri: Option[String],
                            nimi: Map[String, String],
                            oid: String,
                            tila: String) {
  def getLocalizedName(lang: Language): String = {
    val desiredLanguage = List(lang.toString, "fi", "sv", "en") find { k => nimi.get(k).exists(_.nonEmpty) }
    desiredLanguage flatMap { s => nimi.get(s) } getOrElse("?")
  }

  def getHakutyyppi() = {
    if (hakutapaKoodiUri.exists(_.contains("hakutapa_01"))) {
      Yhteishaku
    } else if (hakutapaKoodiUri.exists(_.contains("hakutapa_02"))) {
      Erillishaku
    } else if (hakutapaKoodiUri.exists(_.contains("hakutapa_03"))) {
      JatkuvaHaku
    } else {
      throw new IllegalArgumentException("Unsupported type for haku: " + oid + " - " + hakutapaKoodiUri)
    }
  }
}

object KoutaHaku {
  object Julkaisutila extends Enumeration {
    type Julkaisutila = Value
    val Julkaistu = Value("julkaistu")
  }

  def toHaku(koutaHaku: KoutaHaku, lang: Language, haunAikataulu: Option[HaunAikataulu]): Try[Haku] = {
    for {
      applicationPeriods <- extractApplicationPeriods(koutaHaku)
    } yield Haku(
      aikataulu = haunAikataulu,
      applicationPeriods = applicationPeriods,
      checkBaseEducationConflict = false,
      jarjestelmanHakulomake = false,
      korkeakouluhaku = isKorkeakouluhaku(koutaHaku),
      name = koutaHaku.getLocalizedName(lang),
      oid = koutaHaku.oid,
      published = isPublished(koutaHaku),
      showSingleStudyPlaceEnforcement = false, // FIXME
      siirtohaku = isSiirtohaku(koutaHaku),
      toisenasteenhaku = isToisenasteenhaku(koutaHaku),
      tyyppi = koutaHaku.getHakutyyppi().toString,
      usePriority = false) // FIXME
  }

  private def extractApplicationPeriods(koutaHaku: KoutaHaku): Try[List[Hakuaika]] = {
    Try {
      koutaHaku.hakuajat map { _.toHakuaika } map { _.get } sortBy { _.start }
    } recoverWith {
      case exception: Throwable => Failure(new RuntimeException("Failed to form hakuajat for haku", exception))
    }
  }

  private def isKorkeakouluhaku(koutaHaku: KoutaHaku) = {
    koutaHaku.kohdejoukkoKoodiUri.contains("haunkohdejoukko_12")
  }

  private def isToisenasteenhaku(koutaHaku: KoutaHaku) = {
    val kohdejoukot = List("haunkohdejoukko_11","haunkohdejoukko_17","haunkohdejoukko_20")
    kohdejoukot.exists(koutaHaku.kohdejoukkoKoodiUri.contains(_))
  }

  private def isPublished(koutaHaku: KoutaHaku): Boolean = {
    Julkaisutila.Julkaistu.toString.equals(koutaHaku.tila)
  }

  private def isSiirtohaku(koutaHaku: KoutaHaku) = {
    koutaHaku.kohdejoukonTarkenneKoodiUri.exists(_.contains("haunkohdejoukontarkenne_1#"))
  }
}
