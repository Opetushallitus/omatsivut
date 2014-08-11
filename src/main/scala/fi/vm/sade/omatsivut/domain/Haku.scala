package fi.vm.sade.omatsivut.domain

import org.joda.time.{Interval, DateTime}

case class Haku(oid: String, name: String, applicationPeriods: List[HakuAika], korkeakouluhaku: Boolean, results: Option[Tulokset] = None)
case class HakuAika(start: DateTime, end: DateTime, active: Boolean)
case class Tulokset(start: DateTime, end: DateTime)

object HakuAika {
  def apply(start: DateTime, end: DateTime) = {
    new HakuAika(start, end, new Interval(start, end).containsNow())
  }
}