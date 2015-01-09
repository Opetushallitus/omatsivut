package fi.vm.sade.omatsivut.tarjonta

import fi.vm.sade.omatsivut.ohjausparametrit.domain.HaunAikataulu
import fi.vm.sade.omatsivut.domain.Language._
import org.joda.time.Interval

package object domain {
  case class Haku(oid: String, name: String, applicationPeriods: List[Hakuaika], tyyppi: String, korkeakouluhaku: Boolean, usePriority: Boolean, aikataulu: Option[HaunAikataulu] = None) {
    def active: Boolean = new Interval(applicationPeriods.head.start, applicationPeriods.last.end).containsNow()
  }

  object Haku {
    def apply(tarjontaHaku: TarjontaHaku, lang: Language) : Haku = {
      Haku(tarjontaHaku.oid, tarjontaHaku.nimi("kieli_" + lang.toString), tarjontaHaku.hakuaikas.sortBy(_.alkuPvm).map(h => Hakuaika(h)), HakuTyyppi(tarjontaHaku).toString, isKorkeakouluhaku(tarjontaHaku), tarjontaHaku.usePriority)
    }

    private def isKorkeakouluhaku(tarjontaHaku: TarjontaHaku) = {
      tarjontaHaku.kohdejoukkoUri.contains("haunkohdejoukko_12")
    }
  }

  case class Hakuaika(id: String, start: Long, end: Long) {
    def active = new Interval(start, end).containsNow()
  }

  object Hakuaika {
    def apply(tarjontaHakuaika: TarjontaHakuaika) : Hakuaika = {
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
      if(tarjontaHaku.hakutyyppiUri.contains("hakutyyppi_03")) {
        Lisahaku
      } else {
        if(tarjontaHaku.hakutapaUri.contains("hakutapa_01")) {
          Yhteishaku
        } else if (tarjontaHaku.hakutapaUri.contains("hakutapa_02")) {
          Erillishaku
        } else if(tarjontaHaku.hakutapaUri.contains("hakutapa_03")) {
          JatkuvaHaku
        } else {
          throw new IllegalArgumentException("Unsupported type for haku: " + tarjontaHaku.oid + " - " + tarjontaHaku.hakutyyppiUri + "," + tarjontaHaku.hakutapaUri)
        }
      }
    }
  }
}
