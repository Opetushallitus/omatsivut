package fi.vm.sade.hakemuseditori.tarjonta.kouta

import fi.vm.sade.hakemuseditori.tarjonta.domain.{Hakukohde, KohteenHakuaika, KoulutuksenAlkaminen}
import fi.vm.sade.hakemuseditori.tarjonta.vanha.YhdenPaikanSaanto

import scala.util.{Failure, Success, Try}

sealed case class KoutaHakukohde(alkamiskausiKoodiUri: Option[String],
                                 alkamisvuosi: Option[String],
                                 kaytetaanHaunAikataulua: Option[Boolean],
                                 kaytetaanHaunAlkamiskautta: Option[Boolean],
                                 hakuajat: List[KoutaHakuaika],
                                 oid: String,
                                 yhdenPaikanSaanto: YhdenPaikanSaanto) {
}

object KoutaHakukohde {
  def toHakukohde(koutaHakukohde: KoutaHakukohde): Try[Hakukohde] = {
    for {
      kohteenHakuaika <- extractKohteenHakuajat(koutaHakukohde)
      koulutuksenAlkaminen <- extractKoulutuksenAlkaminen(koutaHakukohde)
    } yield Hakukohde(hakuaikaId = Some("kouta-hakuaika-id"),
      koulutuksenAlkaminen = koulutuksenAlkaminen,
      hakukohdekohtaisetHakuajat = kohteenHakuaika,
      ohjeetUudelleOpiskelijalle = None, // FIXME
      oid = koutaHakukohde.oid,
      yhdenPaikanSaanto = koutaHakukohde.yhdenPaikanSaanto.voimassa)
  }

  private def extractKohteenHakuajat(koutaHakukohde: KoutaHakukohde) : Try[Option[List[KohteenHakuaika]]] = {
    if (koutaHakukohde.kaytetaanHaunAikataulua.getOrElse(false))
      Success(None)
    else
      koutaHakukohde.hakuajat.foldRight[Try[Option[List[KohteenHakuaika]]]](Success(Some(Nil))) {
        case (koutaHakuaika, Success(Some(hs))) => toKohteenHakuaika(koutaHakuaika).map(aika => Some(aika :: hs))
        case (_, failure) => failure
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
