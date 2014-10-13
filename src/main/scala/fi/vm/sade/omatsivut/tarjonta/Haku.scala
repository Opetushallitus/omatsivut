package fi.vm.sade.omatsivut.tarjonta

import fi.vm.sade.omatsivut.ohjausparametrit.domain.Tulosaikataulu
import fi.vm.sade.omatsivut.tarjonta.HaunTyyppi.HaunTyyppi
import org.joda.time.Interval

object Haku {
  def apply(tarjontaHaku: TarjontaHaku) : Haku = {
    Haku(tarjontaHaku.oid, tarjontaHaku.hakuaikas.map(h => Hakuaika(h)), HaunTyyppi(tarjontaHaku), isKorkeakouluhaku(tarjontaHaku))
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

case class Haku(oid: String, hakuajat: List[Hakuaika], tyyppi: HaunTyyppi, korkeakouluHaku: Boolean, tulosaikataulu: Option[Tulosaikataulu] = None)
case class Hakuaika(id: String, start: Long, end: Long, active: Boolean)

object HaunTyyppi extends Enumeration {
  type HaunTyyppi = Value
  val Yhteishaku, Erillishaku, JatkuvaHaku, Lisahaku = Value

  def apply(tarjontaHaku: TarjontaHaku) = {
    if(tarjontaHaku.hakutyyppiUri == "hakutyyppi_03") {
      Lisahaku
    } else {
      if(tarjontaHaku.hakutapaUri == "hakutapa_01") {
        Yhteishaku
      } else if (tarjontaHaku.hakutapaUri == "hakutapa_02") {
        Erillishaku
      } else if(tarjontaHaku.hakutapaUri == "hakutapa_03") {
        JatkuvaHaku
      }
      throw new IllegalArgumentException("Unsupported type for haku: " + tarjontaHaku.oid + " - " + tarjontaHaku.hakutyyppiUri + "," + tarjontaHaku.hakutapaUri)
    }
  }
}