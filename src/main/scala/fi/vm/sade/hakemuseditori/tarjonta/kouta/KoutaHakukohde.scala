package fi.vm.sade.hakemuseditori.tarjonta.kouta

import fi.vm.sade.hakemuseditori.tarjonta.domain.Hakukohde

sealed case class KoutaHakukohde(kaytetaanHaunAikataulua: Option[Boolean],
                                 hakuajat: List[KoutaHakuaika],
                                 oid: String) {
}

object KoutaHakukohde {
  def toHakukohde(koutaHakukohde: KoutaHakukohde): Hakukohde = {
    Hakukohde(hakuaikaId = Some("kouta-hakuaika-id"), // FIXME
      koulutuksenAlkaminen = None,
      kohteenHakuaika = koutaHakukohde.hakuajat.headOption map { _.toKohteenHakuaika }, // FIXME: tuki useammalle hakuajalle
      ohjeetUudelleOpiskelijalle = None,
      oid = koutaHakukohde.oid)
  }
}
