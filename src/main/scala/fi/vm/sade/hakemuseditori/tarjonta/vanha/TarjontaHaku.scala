package fi.vm.sade.hakemuseditori.tarjonta.vanha

import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.tarjonta.domain.Haku
import fi.vm.sade.hakemuseditori.tarjonta.domain.HakuTyyppi.{Erillishaku, JatkuvaHaku, Lisahaku, Yhteishaku}
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

  def toHaku(tarjontaHaku: TarjontaHaku, lang: Language): Haku = {
    Haku(applicationPeriods = tarjontaHaku.hakuaikas.sortBy(_.alkuPvm).map(h => TarjontaHakuaika.toHakuaika(h)),
      checkBaseEducationConflict = checkeBaseEducationConflict(tarjontaHaku),
      jarjestelmanHakulomake = tarjontaHaku.jarjestelmanHakulomake,
      korkeakouluhaku = isKorkeakouluhaku(tarjontaHaku),
      name = tarjontaHaku.getLocalizedName(lang),
      oid = tarjontaHaku.oid,
      published = isPublished(tarjontaHaku),
      showSingleStudyPlaceEnforcement = tarjontaHaku.yhdenPaikanSaanto.voimassa,
      siirtohaku = tarjontaHaku.kohdejoukonTarkenne.exists(_.contains("haunkohdejoukontarkenne_1#")),
      toisenasteenhaku = isToisenasteenhaku(tarjontaHaku),
      tyyppi = tarjontaHaku.getHakutyyppi().toString(),
      usePriority = tarjontaHaku.usePriority)
  }

  private def isKorkeakouluhaku(tarjontaHaku: TarjontaHaku) = {
    tarjontaHaku.kohdejoukkoUri.contains("haunkohdejoukko_12")
  }

  private def isPublished(tarjontaHaku: TarjontaHaku): Boolean = {
    TarjontaTila.JULKAISTU.toString.equals(tarjontaHaku.tila)
  }

  private def isToisenasteenhaku(tarjontaHaku: TarjontaHaku) = {
    val kohdejoukot = List("haunkohdejoukko_11","haunkohdejoukko_17","haunkohdejoukko_20")
    kohdejoukot.exists(tarjontaHaku.kohdejoukkoUri.contains(_))
  }

  private def checkeBaseEducationConflict(tarjontaHaku: TarjontaHaku): Boolean = {
    isKorkeakouluhaku(tarjontaHaku) && tarjontaHaku.kohdejoukonTarkenne.getOrElse("").trim.isEmpty
  }
}

sealed case class YhdenPaikanSaanto(voimassa: Boolean, syy: String)
