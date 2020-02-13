package fi.vm.sade.hakemuseditori.tarjonta.kouta

import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, Hakuaika}
import fi.vm.sade.hakemuseditori.tarjonta.domain.HakuTyyppi.{Erillishaku, JatkuvaHaku, Yhteishaku}

import scala.util.Try

sealed case class KoutaHaku(hakuajat: List[KoutaHakuaika],
                            hakutapaKoodiUri: Option[String],
                            kohdejoukkoKoodiUri: String,
                            kohdejoukonTarkenneKoodiUri: Option[String],
                            nimi: Map[String, String],
                            oid: String,
                            tila: String) {
  def getLocalizedName(lang: Language): String = {
    nimi.get(lang.toString).orElse(nimi.get("fi")).getOrElse("?")
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
  def toHaku(koutaHaku: KoutaHaku, lang: Language): Try[Haku] = {
    for {
      applicationPeriods <- extractApplicationPeriods(koutaHaku)
    } yield Haku(applicationPeriods = applicationPeriods,
      checkBaseEducationConflict = checkBaseEducationConflict(koutaHaku),
      jarjestelmanHakulomake = false,
      korkeakouluhaku = isKorkeakouluhaku(koutaHaku),
      name = koutaHaku.getLocalizedName(lang),
      oid = koutaHaku.oid,
      showSingleStudyPlaceEnforcement = false, // FIXME
      siirtohaku = isSiirtohaku(koutaHaku),
      tila = koutaHaku.tila,
      toisenasteenhaku = isToisenasteenhaku(koutaHaku),
      tyyppi = koutaHaku.getHakutyyppi().toString,
      usePriority = false) // FIXME
  }

  private def extractApplicationPeriods(koutaHaku: KoutaHaku): Try[List[Hakuaika]] = {
    Try(koutaHaku.hakuajat map { _.toHakuaika } map { _.get })
  }

  private def isKorkeakouluhaku(koutaHaku: KoutaHaku) = {
    koutaHaku.kohdejoukkoKoodiUri.contains("haunkohdejoukko_12")
  }

  private def isToisenasteenhaku(koutaHaku: KoutaHaku) = {
    val kohdejoukot = List("haunkohdejoukko_11","haunkohdejoukko_17","haunkohdejoukko_20")
    kohdejoukot.exists(koutaHaku.kohdejoukkoKoodiUri.contains(_))
  }

  private def checkBaseEducationConflict(koutaHaku: KoutaHaku): Boolean = {
    isKorkeakouluhaku(koutaHaku) && koutaHaku.kohdejoukonTarkenneKoodiUri.getOrElse("").trim.isEmpty
  }

  private def isSiirtohaku(koutaHaku: KoutaHaku) = {
    koutaHaku.kohdejoukonTarkenneKoodiUri.exists(_.contains("haunkohdejoukontarkenne_1#"))
  }
}
