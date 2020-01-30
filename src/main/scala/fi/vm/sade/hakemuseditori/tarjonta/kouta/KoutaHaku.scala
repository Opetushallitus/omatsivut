package fi.vm.sade.hakemuseditori.tarjonta.kouta

import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.tarjonta.domain.Haku

sealed case class KoutaHaku(hakuajat: List[KoutaHakuaika], nimi: Map[String, String], oid: String, tila: String) {
  def getLocalizedName(lang: Language): String = {
    nimi.get(lang.toString).orElse(nimi.get("fi")).getOrElse("?")
  }
}

object KoutaHaku {
  def toHaku(koutaHaku: KoutaHaku, lang: Language): Haku = {
    Haku(applicationPeriods = koutaHaku.hakuajat map { _.toHakuaika },
      checkBaseEducationConflict = false,
      jarjestelmanHakulomake = false,
      korkeakouluhaku = false,
      name = koutaHaku.getLocalizedName(lang),
      oid = koutaHaku.oid,
      showSingleStudyPlaceEnforcement = false,
      siirtohaku = false,
      tila = koutaHaku.tila,
      toisenasteenhaku = false,
      tyyppi = "",
      usePriority = false)
  }
}
