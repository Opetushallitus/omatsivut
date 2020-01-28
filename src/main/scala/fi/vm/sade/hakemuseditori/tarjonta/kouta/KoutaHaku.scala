package fi.vm.sade.hakemuseditori.tarjonta.kouta

import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.tarjonta.domain.Haku

sealed case class KoutaHaku(oid: String, nimi: Map[String, String], tila: String) {
  def getLocalizedName(lang: Language): String = {
    nimi.get(lang.toString).orElse(nimi.get("fi")).getOrElse("?")
  }
}

object KoutaHaku {
  def toHaku(koutaHaku: KoutaHaku, lang: Language): Haku = {
    Haku(oid = koutaHaku.oid,
      tila = koutaHaku.tila,
      name = koutaHaku.getLocalizedName(lang),
      applicationPeriods = List.empty,
      tyyppi = "",
      korkeakouluhaku = false,
      showSingleStudyPlaceEnforcement = false,
      siirtohaku = false,
      checkBaseEducationConflict = false,
      usePriority = false,
      jarjestelmanHakulomake = false,
      toisenasteenhaku = false)
  }
}
