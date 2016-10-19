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

sealed case class YhdenPaikanSaanto(voimassa: Boolean, syy: String)
