package fi.vm.sade.hakemuseditori.tarjonta.kouta

import fi.vm.sade.hakemuseditori.tarjonta.domain.{Hakukohde, KohteenHakuaika, KoulutuksenAlkaminen}

import scala.util.{Success, Try}

sealed case class KoutaHakukohde(alkamiskausiKoodiUri: Option[String],
                                 alkamisvuosi: Option[String],
                                 kaytetaanHaunAikataulua: Option[Boolean],
                                 kaytetaanHaunAlkamiskautta: Option[Boolean],
                                 hakuajat: List[KoutaHakuaika],
                                 oid: String) {
}

object KoutaHakukohde {
  def toHakukohde(koutaHakukohde: KoutaHakukohde): Try[Hakukohde] = {
    for {
      kohteenHakuaika <- extractKohteenHakuaika(koutaHakukohde)
      koulutuksenAlkaminen <- extractKoulutuksenAlkaminen(koutaHakukohde)
    } yield Hakukohde(hakuaikaId = Some("kouta-hakuaika-id"), // FIXME
      koulutuksenAlkaminen = koulutuksenAlkaminen,
      kohteenHakuaika = kohteenHakuaika, // FIXME: tuki useammalle hakuajalle
      ohjeetUudelleOpiskelijalle = None,
      oid = koutaHakukohde.oid)
  }

  private def extractKohteenHakuaika(koutaHakukohde: KoutaHakukohde) : Try[Option[KohteenHakuaika]] = {
    if (koutaHakukohde.kaytetaanHaunAikataulua.getOrElse(false))
      Success(None)
    else
      koutaHakukohde.hakuajat.headOption match {
        case Some(koutaHakuaika) => koutaHakuaika.toKohteenHakuaika map ( x => Some(x) )
        case None => Success(None)
      }
  }

  private def extractKoulutuksenAlkaminen(koutaHakukohde: KoutaHakukohde): Try[Option[KoulutuksenAlkaminen]] = {
    if (koutaHakukohde.kaytetaanHaunAlkamiskautta.getOrElse(false))
      Success(None)
    else
      createKoulutuksenAlkaminen(koutaHakukohde.alkamiskausiKoodiUri, koutaHakukohde.alkamisvuosi)
  }

  private def createKoulutuksenAlkaminen(alkamiskausiKoodiUri: Option[String],
                                         alkamisvuosi: Option[String]) = Try {
    for {
      alkamiskausiKoodiUri <- alkamiskausiKoodiUri
      alkamisvuosi <- alkamisvuosi map ( _.toInt )
    } yield KoulutuksenAlkaminen(alkamisvuosi, alkamiskausiKoodiUri)
  }
}
