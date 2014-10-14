package fi.vm.sade.omatsivut.tarjonta

import fi.vm.sade.omatsivut.ohjausparametrit.domain.Tulosaikataulu
import fi.vm.sade.omatsivut.tarjonta.HaunTyyppi.HaunTyyppi
import org.joda.time.Interval

object Haku {
  def apply(tarjontaHaku: TarjontaHaku) : Haku = {
    Haku(tarjontaHaku.oid, tarjontaHaku.hakuaikas.map(h => Hakuaika(h)), HaunTyyppi(tarjontaHaku).toString, isKorkeakouluhaku(tarjontaHaku))
  }

  private def isKorkeakouluhaku(tarjontaHaku: TarjontaHaku) = {
    tarjontaHaku.kohdejoukkoUri == "haunkohdejoukko_12"
  }
}

object Hakuaika {
  def apply(tarjontaHakuaika: TarjontaHakuaika) : Hakuaika = {
    Hakuaika(tarjontaHakuaika.hakuaikaId, tarjontaHakuaika.alkuPvm, tarjontaHakuaika.loppuPvm, new Interval(tarjontaHakuaika.alkuPvm, tarjontaHakuaika.loppuPvm).containsNow())
  }

  def apply(id: String, start: Long, end: Long): Unit = {
    Hakuaika(id, start, end, new Interval(start, end).containsNow())
  }
}

case class Haku(oid: String, hakuajat: List[Hakuaika], tyyppi: String, korkeakouluHaku: Boolean, tulosaikataulu: Option[Tulosaikataulu] = None)
case class Hakuaika(id: String, start: Long, end: Long, active: Boolean)

object HaunTyyppi extends Enumeration {
  type HaunTyyppi = Value
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