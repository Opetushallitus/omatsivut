package fi.vm.sade.hakemuseditori.tarjonta.domain

import fi.vm.sade.hakemuseditori.domain.Language._

sealed case class KoutaHaku(oid: String, nimi: Map[String, String], tila: String) {
  def getLocalizedName(lang: Language): String = {
    nimi.get(lang.toString).orElse(nimi.get("fi")).getOrElse("?")
  }
}
