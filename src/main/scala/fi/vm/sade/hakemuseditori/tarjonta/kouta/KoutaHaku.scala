package fi.vm.sade.hakemuseditori.tarjonta.kouta

import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.ohjausparametrit.domain.HaunAikataulu
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, Hakuaika}
import fi.vm.sade.hakemuseditori.tarjonta.domain.HakuTyyppi.{Erillishaku, JatkuvaHaku, Lisahaku, Yhteishaku}
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig

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
    } else if (hakutapaKoodiUri.exists(_.contains("hakutapa_04"))) {
      Erillishaku
    } else if (hakutapaKoodiUri.exists(_.contains("hakutapa_05"))) {
      Erillishaku
    } else if (hakutapaKoodiUri.exists(_.contains("hakutapa_06"))) {
      Lisahaku
    } else {
      throw new IllegalArgumentException("Unsupported type for haku: " + oid + " - " + hakutapaKoodiUri)
    }
  }
}

object KoutaHaku {
  def toHaku(koutaHaku: KoutaHaku,
             lang: Language,
             haunAikataulu: Option[HaunAikataulu],
             config: AppConfig): Try[Haku] = {
    for {
      applicationPeriods <- extractApplicationPeriods(koutaHaku)
    } yield Haku(
      aikataulu = haunAikataulu,
      applicationPeriods = applicationPeriods,
      checkBaseEducationConflict = false,
      jarjestelmanHakulomake = false,
      korkeakouluhaku = isKorkeakouluhaku(koutaHaku, config),
      name = koutaHaku.getLocalizedName(lang),
      oid = koutaHaku.oid,
      published = isPublished(koutaHaku),
      siirtohaku = isSiirtohaku(koutaHaku, config),
      toisenasteenhaku = isToisenasteenhaku(koutaHaku, config),
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

  private def isKorkeakouluhaku(koutaHaku: KoutaHaku, config: AppConfig) = {
    config.settings.kohdejoukotKorkeakoulu.exists(s => koutaHaku.kohdejoukkoKoodiUri.startsWith(s + "#"))
  }

  private def isToisenasteenhaku(koutaHaku: KoutaHaku, config: AppConfig) = {
    config.settings.kohdejoukotToinenAste.exists(s => koutaHaku.kohdejoukkoKoodiUri.startsWith(s + "#"))
  }

  private def isPublished(koutaHaku: KoutaHaku): Boolean = {
    "julkaistu".equals(koutaHaku.tila) || "arkistoitu".equals(koutaHaku.tila)
  }

  private def isSiirtohaku(koutaHaku: KoutaHaku, config: AppConfig) = {
    config.settings.kohdejoukonTarkenteetSiirtohaku.exists(s =>
      koutaHaku.kohdejoukonTarkenneKoodiUri.exists(_.startsWith(s + "#")))
  }
}
