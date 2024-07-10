package fi.vm.sade.hakemuseditori.tarjonta.domain

import fi.vm.sade.hakemuseditori.hakemus.domain.Hakutoive
import fi.vm.sade.hakemuseditori.ohjausparametrit.domain.HaunAikataulu
import org.joda.time.LocalDateTime

case class Haku(oid: String, published: Boolean, name: String, applicationPeriods: List[Hakuaika], tyyppi: String,
                korkeakouluhaku: Boolean, siirtohaku: Boolean,
                checkBaseEducationConflict: Boolean, usePriority: Boolean, jarjestelmanHakulomake: Boolean,
                toisenasteenhaku: Boolean, aikataulu: Option[HaunAikataulu]) {
  def hakukierrosvoimassa: Boolean = new LocalDateTime().isBefore(aikataulu.flatMap(_.hakukierrosPaattyy).map(new LocalDateTime(_: Long)).getOrElse(new LocalDateTime().minusYears(100)))
}

case class Hakuaika(id: String, start: Long, end: Option[Long]) {
  def ended(now: Long): Boolean = end.exists(_ <= now)
  def active(now: Long): Boolean = start <= now && !ended(now)
  def active: Boolean = active(LocalDateTime.now().toDate.getTime)

}

case class Hakukohde(oid: String,
                     nimi: String,
                     hakuaikaId: Option[String],
                     koulutuksenAlkaminen: Option[KoulutuksenAlkaminen],
                     hakukohdekohtaisetHakuajat: Option[List[KohteenHakuaika]],
                     ohjeetUudelleOpiskelijalle: Option[String],
                     yhdenPaikanSaanto: Boolean)

case class KohteenHakuaika(start: Long, end: Option[Long]) {
  def ended(now: Long): Boolean = end.exists(_ <= now)
  def active(now: Long): Boolean = start <= now && !ended(now)
  def active: Boolean = active(LocalDateTime.now().toDate.getTime)
}

object KohteenHakuaika {
  def hakuaikaEnded(haku: Haku, hakukohde: Hakukohde, now: Long): Boolean = {
    hakukohde.hakuaikaId
      .map(id => haku.applicationPeriods.exists(hakuaika => hakuaika.id == id && hakuaika.ended(now)))
      .getOrElse(hakukohde.hakukohdekohtaisetHakuajat.exists(_.exists(_.ended(now))))
  }
  def hakuaikaEnded(haku: Haku, hakutoive: Hakutoive, now: Long): Boolean = {
    hakutoive.hakuaikaId
      .map(id => haku.applicationPeriods.exists(hakuaika => hakuaika.id == id && hakuaika.ended(now)))
      .getOrElse(hakutoive.hakukohdekohtaisetHakuajat.exists(_.exists(_.ended(now))))
  }
  def active(haku: Haku, hakukohde: Hakukohde, now: Long): Boolean = {
    hakukohde.hakuaikaId
      .map(id => haku.applicationPeriods.exists(hakuaika => hakuaika.id == id && hakuaika.active(now)))
      .getOrElse(hakukohde.hakukohdekohtaisetHakuajat.exists(_.exists(_.active(now))))
  }
}

case class KoulutuksenAlkaminen(vuosi: Long, kausiUri: String)

object HakuTyyppi extends Enumeration {
  type HakuTyyppi = Value
  val Yhteishaku = Value("YHTEISHAKU")
  val Erillishaku = Value("ERILLISHAKU")
  val JatkuvaHaku = Value("JATKUVA_HAKU")
  val Lisahaku = Value("LISAHAKU")
}
