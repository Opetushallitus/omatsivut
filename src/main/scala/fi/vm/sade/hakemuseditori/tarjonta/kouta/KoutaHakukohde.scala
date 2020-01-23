package fi.vm.sade.hakemuseditori.tarjonta.kouta

import fi.vm.sade.hakemuseditori.tarjonta.domain.Hakukohde

sealed case class KoutaHakukohde(oid: String) {
}

object KoutaHakukohde {
  def toHakukohde(koutaHakukohde: KoutaHakukohde): Hakukohde = {
    Hakukohde(
      oid = koutaHakukohde.oid,
      hakuaikaId = Some(""),
      koulutuksenAlkaminen = None,
      kohteenHakuaika = None,
      ohjeetUudelleOpiskelijalle = None)
  }
}
