package fi.vm.sade.hakemuseditori.tarjonta.vanha

import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.ohjausparametrit.domain.HaunAikataulu
import fi.vm.sade.hakemuseditori.tarjonta.domain.Haku
import fi.vm.sade.hakemuseditori.tarjonta.domain.HakuTyyppi.{Erillishaku, JatkuvaHaku, Lisahaku, Yhteishaku}
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.tarjonta.shared.types.TarjontaTila

sealed case class TarjontaHaku(oid: String, hakuaikas: List[TarjontaHakuaika], hakutapaUri: String, hakutyyppiUri: String,
                               kohdejoukkoUri: String, kohdejoukonTarkenne: Option[String], usePriority: Boolean,
                               yhdenPaikanSaanto: YhdenPaikanSaanto,
                               nimi: Map[String, String], tila: String, jarjestelmanHakulomake: Boolean) {
  def getLocalizedName(lang: Language): String = {
    nimi.get("kieli_" + lang.toString).orElse(nimi.get("kieli_fi")).getOrElse("?")
  }

  def getHakutyyppi() = {
    if (hakutyyppiUri.contains("hakutyyppi_03")) {
      Lisahaku
    } else {
      if (hakutapaUri.contains("hakutapa_01")) {
        Yhteishaku
      } else if (hakutapaUri.contains("hakutapa_02")) {
        Erillishaku
      } else if (hakutapaUri.contains("hakutapa_03")) {
        JatkuvaHaku
      } else {
        throw new IllegalArgumentException("Unsupported type for haku: " + oid + " - " + hakutyyppiUri + "," + hakutapaUri)
      }
    }
  }
}

object TarjontaHaku {

  def toHaku(tarjontaHaku: TarjontaHaku,
             lang: Language,
             haunAikataulu: Option[HaunAikataulu],
             config: AppConfig): Haku = {
    Haku(
      aikataulu = haunAikataulu,
      applicationPeriods = tarjontaHaku.hakuaikas.sortBy(_.alkuPvm).map(h => TarjontaHakuaika.toHakuaika(h)),
      checkBaseEducationConflict = checkeBaseEducationConflict(tarjontaHaku, config),
      jarjestelmanHakulomake = tarjontaHaku.jarjestelmanHakulomake,
      korkeakouluhaku = isKorkeakouluhaku(tarjontaHaku, config),
      name = tarjontaHaku.getLocalizedName(lang),
      oid = tarjontaHaku.oid,
      published = isPublished(tarjontaHaku),
      siirtohaku = isSiirtohaku(tarjontaHaku, config),
      toisenasteenhaku = isToisenasteenhaku(tarjontaHaku,config),
      tyyppi = tarjontaHaku.getHakutyyppi().toString,
      usePriority = tarjontaHaku.usePriority)
  }

  private def isPublished(tarjontaHaku: TarjontaHaku): Boolean = {
    TarjontaTila.JULKAISTU.toString.equals(tarjontaHaku.tila)
  }

  private def isKorkeakouluhaku(tarjontaHaku: TarjontaHaku, config: AppConfig) = {
    config.settings.kohdejoukotKorkeakoulu.exists(s => tarjontaHaku.kohdejoukkoUri.startsWith(s + "#"))
  }

  private def isToisenasteenhaku(tarjontaHaku: TarjontaHaku, config: AppConfig) = {
    config.settings.kohdejoukotToinenAste.exists(s => tarjontaHaku.kohdejoukkoUri.startsWith(s + "#"))
  }

  private def isSiirtohaku(tarjontaHaku: TarjontaHaku, config: AppConfig) = {
    config.settings.kohdejoukonTarkenteetSiirtohaku.exists(s =>
      tarjontaHaku.kohdejoukonTarkenne.exists(_.startsWith(s + "#")))
  }

  private def checkeBaseEducationConflict(tarjontaHaku: TarjontaHaku, config: AppConfig): Boolean = {
    isKorkeakouluhaku(tarjontaHaku, config) && tarjontaHaku.kohdejoukonTarkenne.getOrElse("").trim.isEmpty
  }
}

sealed case class YhdenPaikanSaanto(voimassa: Boolean, syy: String)
