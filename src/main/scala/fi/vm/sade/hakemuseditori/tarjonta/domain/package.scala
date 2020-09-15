package fi.vm.sade.hakemuseditori.tarjonta.domain

import java.util.Date

import fi.vm.sade.hakemuseditori.hakemus.domain.Hakutoive
import fi.vm.sade.hakemuseditori.ohjausparametrit.domain.HaunAikataulu
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationPeriod
import org.joda.time.{Interval, LocalDateTime}

case class Haku(oid: String, published: Boolean, name: String, applicationPeriods: List[Hakuaika], tyyppi: String,
                korkeakouluhaku: Boolean, showSingleStudyPlaceEnforcement: Boolean, siirtohaku: Boolean,
                checkBaseEducationConflict: Boolean, usePriority: Boolean, jarjestelmanHakulomake: Boolean,
                toisenasteenhaku: Boolean, aikataulu: Option[HaunAikataulu]) {
  def hakukierrosvoimassa: Boolean = new LocalDateTime().isBefore(aikataulu.flatMap(_.hakukierrosPaattyy).map(new LocalDateTime(_: Long)).getOrElse(new LocalDateTime().minusYears(100)))
}

case class Hakuaika(id: String, start: Long, end: Option[Long]) {
  def ended(now: Long): Boolean = end.exists(_ <= now)
  def active(now: Long): Boolean = start <= now && !ended(now)
  def active: Boolean = active(LocalDateTime.now().toDate.getTime)
  def toApplicationPeriod: ApplicationPeriod = {
    new ApplicationPeriod(new Date(start), end.map(new Date(_)).orNull)
  }
}

case class Hakukohde(oid: String, hakuaikaId: Option[String], koulutuksenAlkaminen: Option[KoulutuksenAlkaminen],
                     kohteenHakuaika: Option[KohteenHakuaika], ohjeetUudelleOpiskelijalle: Option[String])

case class KohteenHakuaika(start: Long, end: Option[Long]) {
  def ended(now: Long): Boolean = end.exists(_ <= now)
  def active(now: Long): Boolean = start <= now && !ended(now)
  def active: Boolean = active(LocalDateTime.now().toDate.getTime)
}

object KohteenHakuaika {
  def hakuaikaEnded(haku: Haku, hakukohde: Hakukohde, now: Long): Boolean = {
    hakukohde.hakuaikaId
      .map(id => haku.applicationPeriods.exists(hakuaika => hakuaika.id == id && hakuaika.ended(now)))
      .getOrElse(hakukohde.kohteenHakuaika.exists(_.ended(now)))
  }
  def hakuaikaEnded(haku: Haku, hakutoive: Hakutoive, now: Long): Boolean = {
    hakutoive.hakuaikaId
      .map(id => haku.applicationPeriods.exists(hakuaika => hakuaika.id == id && hakuaika.ended(now)))
      .getOrElse(hakutoive.kohdekohtainenHakuaika.exists(_.ended(now)))
  }
  def active(haku: Haku, hakukohde: Hakukohde, now: Long): Boolean = {
    hakukohde.hakuaikaId
      .map(id => haku.applicationPeriods.exists(hakuaika => hakuaika.id == id && hakuaika.active(now)))
      .getOrElse(hakukohde.kohteenHakuaika.exists(_.active(now)))
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
