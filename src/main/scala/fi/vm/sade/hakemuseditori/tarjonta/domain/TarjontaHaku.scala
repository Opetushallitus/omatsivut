package fi.vm.sade.hakemuseditori.tarjonta.domain

import fi.vm.sade.hakemuseditori.domain.Language._

sealed case class TarjontaHaku(oid: String, hakuaikas: List[TarjontaHakuaika], hakutapaUri: String, hakutyyppiUri: String,
                               kohdejoukkoUri: String, kohdejoukonTarkenne: Option[String], usePriority: Boolean,
                               yhdenPaikanSaanto: YhdenPaikanSaanto,
                               nimi: Map[String, String], tila: String, jarjestelmanHakulomake: Boolean) {
  def getLocalizedName(lang: Language): String = {
    nimi.get("kieli_" + lang.toString).orElse(nimi.get("kieli_fi")).getOrElse("?")
  }
}

object TarjontaHaku {

  def toHaku(tarjontaHaku: TarjontaHaku, lang: Language): Haku = {
    Haku(tarjontaHaku.oid, tarjontaHaku.tila, tarjontaHaku.getLocalizedName(lang), tarjontaHaku.hakuaikas.sortBy(_.alkuPvm).map(h => Hakuaika(h)),
      HakuTyyppi(tarjontaHaku).toString, isKorkeakouluhaku(tarjontaHaku), tarjontaHaku.yhdenPaikanSaanto.voimassa,
      tarjontaHaku.kohdejoukonTarkenne.exists(_.contains("haunkohdejoukontarkenne_1#")),
      checkeBaseEducationConflict(tarjontaHaku), tarjontaHaku.usePriority, tarjontaHaku.jarjestelmanHakulomake,
      isToisenasteenhaku(tarjontaHaku)
    )
  }

  private def isKorkeakouluhaku(tarjontaHaku: TarjontaHaku) = {
    tarjontaHaku.kohdejoukkoUri.contains("haunkohdejoukko_12")
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
