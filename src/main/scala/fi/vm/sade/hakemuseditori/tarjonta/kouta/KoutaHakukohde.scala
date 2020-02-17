package fi.vm.sade.hakemuseditori.tarjonta.kouta

import fi.vm.sade.hakemuseditori.tarjonta.domain.{Hakukohde, KohteenHakuaika, KoulutuksenAlkaminen}

import scala.util.{Failure, Success, Try}

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
      ohjeetUudelleOpiskelijalle = None, // FIXME
      oid = koutaHakukohde.oid)
  }

  private def extractKohteenHakuaika(koutaHakukohde: KoutaHakukohde) : Try[Option[KohteenHakuaika]] = {
    if (koutaHakukohde.kaytetaanHaunAikataulua.getOrElse(false))
      Success(None)
    else
      koutaHakukohde.hakuajat.headOption match {
        case Some(koutaHakuaika) => toKohteenHakuaika(koutaHakuaika) map (x => Some(x))
        case None => Success(None)
      }
  }

  private def toKohteenHakuaika(koutaHakuaika: KoutaHakuaika): Try[KohteenHakuaika] = {
    koutaHakuaika.toKohteenHakuaika
      .recoverWith {
        case exception: Throwable => Failure(new RuntimeException("Failed to form kohteenHakuaika", exception))
      }
  }

  private def extractKoulutuksenAlkaminen(koutaHakukohde: KoutaHakukohde): Try[Option[KoulutuksenAlkaminen]] = {
    if (koutaHakukohde.kaytetaanHaunAlkamiskautta.getOrElse(false))
      Success(None)
    else
      createKoulutuksenAlkaminen(koutaHakukohde)
  }

  private def createKoulutuksenAlkaminen(koutaHakukohde: KoutaHakukohde): Try[Option[KoulutuksenAlkaminen]] = {
    tryToCreateKoulutuksenAlkaminen(koutaHakukohde.alkamiskausiKoodiUri, koutaHakukohde.alkamisvuosi)
      .recoverWith {
        case exception: Throwable => Failure(new RuntimeException("Failed to form koulutuksenAlkaminen", exception))
      }
  }

  private def tryToCreateKoulutuksenAlkaminen(alkamiskausiKoodiUri: Option[String],
                                              alkamisvuosi: Option[String]): Try[Option[KoulutuksenAlkaminen]] = {
    Try(for {
      alkamiskausiKoodiUri <- alkamiskausiKoodiUri
      alkamisvuosi <- alkamisvuosi map ( _.toInt )
    } yield KoulutuksenAlkaminen(alkamisvuosi, alkamiskausiKoodiUri))
  }
}
