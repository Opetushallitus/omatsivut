package fi.vm.sade.omatsivut.tarjonta

import fi.vm.sade.omatsivut.domain.Language.Language
import fi.vm.sade.omatsivut.ohjausparametrit.domain.HaunAikataulu
import org.joda.time.Interval

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