package fi.vm.sade.omatsivut.tarjonta

import org.joda.time.Interval

case class Hakuaika(id: String, start: Long, end: Long) {
  def active = new Interval(start, end).containsNow()
}
object Hakuaika {
  def apply(tarjontaHakuaika: TarjontaHakuaika) : Hakuaika = {
    Hakuaika(tarjontaHakuaika.hakuaikaId, tarjontaHakuaika.alkuPvm, tarjontaHakuaika.loppuPvm)
  }
}