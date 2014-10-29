package fi.vm.sade.omatsivut.tarjonta

import fi.vm.sade.omatsivut.domain.Language.Language
import fi.vm.sade.omatsivut.ohjausparametrit.domain.HaunAikataulu
import org.joda.time.Interval

case class Haku(oid: String, name: String, applicationPeriods: List[Hakuaika], tyyppi: String, korkeakouluHaku: Boolean, aikataulu: Option[HaunAikataulu] = None)
object Haku {
  def apply(tarjontaHaku: TarjontaHaku, lang: Language) : Haku = {
    Haku(tarjontaHaku.oid, tarjontaHaku.nimi("kieli_" + lang.toString), tarjontaHaku.hakuaikas.map(h => Hakuaika(h)), HaunTyyppi(tarjontaHaku).toString, isKorkeakouluhaku(tarjontaHaku))
  }

  private def isKorkeakouluhaku(tarjontaHaku: TarjontaHaku) = {
    tarjontaHaku.kohdejoukkoUri.contains("haunkohdejoukko_12")
  }
}
