package fi.vm.sade.hakemuseditori.tarjonta.kouta

import fi.vm.sade.hakemuseditori.tarjonta.domain.{Hakukohde, KohteenHakuaika}

import scala.util.Try

sealed case class KoutaHakukohde(kaytetaanHaunAikataulua: Option[Boolean],
                                 hakuajat: List[KoutaHakuaika],
                                 oid: String) {
}

object KoutaHakukohde {
  def toHakukohde(koutaHakukohde: KoutaHakukohde): Try[Hakukohde] = {
    extractKohteenHakuaika(koutaHakukohde) map { kohteenHakuaika =>
      Hakukohde(hakuaikaId = Some("kouta-hakuaika-id"), // FIXME
        koulutuksenAlkaminen = None,
        kohteenHakuaika = kohteenHakuaika, // FIXME: tuki useammalle hakuajalle
        ohjeetUudelleOpiskelijalle = None,
        oid = koutaHakukohde.oid)
    }
  }

  private def extractKohteenHakuaika(koutaHakukohde: KoutaHakukohde) : Try[Option[KohteenHakuaika]] = Try {
    if (koutaHakukohde.kaytetaanHaunAikataulua.getOrElse(false))
      None
    else
      koutaHakukohde.hakuajat.headOption map ( _.toKohteenHakuaika )
  }
}
