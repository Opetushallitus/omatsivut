package fi.vm.sade.omatsivut.tarjonta.domain

import fi.vm.sade.omatsivut.domain.Language._

sealed case class TarjontaHaku(oid: String, hakuaikas: List[TarjontaHakuaika],
                        hakutapaUri: String, hakutyyppiUri: String, kohdejoukkoUri: String,
                        usePriority: Boolean, nimi: Map[String, String]) {
  def getLocalizedName(lang: Language): String = {
    nimi.get("kieli_" + lang.toString).orElse(nimi.get("kieli_fi")).getOrElse("?")
  }
}
