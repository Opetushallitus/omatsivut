package fi.vm.sade.omatsivut.tarjonta

import org.joda.time.Interval

case class Hakukohde(oid: String, hakuaikaId: Option[String], kohteenHakuaika: Option[KohteenHakuaika])
case class KohteenHakuaika(start: Long, end: Long) {
  def active = new Interval(start, end).containsNow()
}

