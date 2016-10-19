package fi.vm.sade.hakemuseditori.tarjonta.domain

import java.util.Date

import fi.vm.sade.hakemuseditori.ohjausparametrit.domain.HaunAikataulu
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationPeriod
import fi.vm.sade.tarjonta.shared.types.TarjontaTila
import fi.vm.sade.hakemuseditori.domain.Language._
import org.joda.time.{Interval, LocalDateTime}

case class Haku(oid: String, tila: String, name: String, applicationPeriods: List[Hakuaika], tyyppi: String,
                korkeakouluhaku: Boolean, showSingleStudyPlaceEnforcement: Boolean, siirtohaku: Boolean,
                checkBaseEducationConflict: Boolean, usePriority: Boolean, jarjestelmanHakulomake: Boolean,
                aikataulu: Option[HaunAikataulu] = None) {
  def active: Boolean = new Interval(applicationPeriods.head.start, applicationPeriods.last.end).containsNow()
  def published: Boolean = TarjontaTila.JULKAISTU.toString.equals(tila)
  def hakukierrosvoimassa: Boolean = new LocalDateTime().isBefore(aikataulu.flatMap(_.hakukierrosPaattyy).map(new LocalDateTime(_: Long)).getOrElse(new LocalDateTime().minusYears(100)))
}

object Haku {
  def apply(tarjontaHaku: TarjontaHaku, lang: Language): Haku = {
    Haku(tarjontaHaku.oid, tarjontaHaku.tila, tarjontaHaku.getLocalizedName(lang), tarjontaHaku.hakuaikas.sortBy(_.alkuPvm).map(h => Hakuaika(h)),
      HakuTyyppi(tarjontaHaku).toString, isKorkeakouluhaku(tarjontaHaku), tarjontaHaku.yhdenPaikanSaanto.voimassa,
      tarjontaHaku.kohdejoukonTarkenne.exists(_.contains("haunkohdejoukontarkenne_1#")),
      checkeBaseEducationConflict(tarjontaHaku), tarjontaHaku.usePriority, tarjontaHaku.jarjestelmanHakulomake
    )
  }

  private def isKorkeakouluhaku(tarjontaHaku: TarjontaHaku) = {
    tarjontaHaku.kohdejoukkoUri.contains("haunkohdejoukko_12")
  }

  private def checkeBaseEducationConflict(tarjontaHaku: TarjontaHaku): Boolean = {
    isKorkeakouluhaku(tarjontaHaku) && tarjontaHaku.kohdejoukonTarkenne.getOrElse("").trim.isEmpty
  }
}

case class Hakuaika(id: String, start: Long, end: Long) {
  def active = new Interval(start, end).containsNow()
  def toApplicationPeriod: ApplicationPeriod = {
    new ApplicationPeriod(new Date(start), new Date(end))
  }
}

object Hakuaika {
  def apply(tarjontaHakuaika: TarjontaHakuaika): Hakuaika = {
    Hakuaika(tarjontaHakuaika.hakuaikaId, tarjontaHakuaika.alkuPvm, tarjontaHakuaika.loppuPvm)
  }
}

case class Hakukohde(oid: String, hakuaikaId: Option[String], kohteenHakuaika: Option[KohteenHakuaika])

case class KohteenHakuaika(start: Long, end: Long) {
  def active = new Interval(start, end).containsNow()
}

object HakuTyyppi extends Enumeration {
  type HakuTyyppi = Value
  val Yhteishaku = Value("YHTEISHAKU")
  val Erillishaku = Value("ERILLISHAKU")
  val JatkuvaHaku = Value("JATKUVA_HAKU")
  val Lisahaku = Value("LISAHAKU")

  def apply(tarjontaHaku: TarjontaHaku) = {
    if (tarjontaHaku.hakutyyppiUri.contains("hakutyyppi_03")) {
      Lisahaku
    } else {
      if (tarjontaHaku.hakutapaUri.contains("hakutapa_01")) {
        Yhteishaku
      } else if (tarjontaHaku.hakutapaUri.contains("hakutapa_02")) {
        Erillishaku
      } else if (tarjontaHaku.hakutapaUri.contains("hakutapa_03")) {
        JatkuvaHaku
      } else {
        throw new IllegalArgumentException("Unsupported type for haku: " + tarjontaHaku.oid + " - " + tarjontaHaku.hakutyyppiUri + "," + tarjontaHaku.hakutapaUri)
      }
    }
  }
}