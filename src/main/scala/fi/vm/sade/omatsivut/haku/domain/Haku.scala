package fi.vm.sade.omatsivut.haku.domain

import org.joda.time.Interval

case class Haku(oid: String, name: String, applicationPeriods: List[HakuAika], korkeakouluhaku: Boolean, results: Option[Tulokset] = None)
case class HakuAika(start: Long, end: Long, active: Boolean)
case class Tulokset(start: Long, end: Long)

object HakuAika {
  def apply(start: Long, end: Long) = {
    new HakuAika(start, end, new Interval(start, end).containsNow())
  }
}