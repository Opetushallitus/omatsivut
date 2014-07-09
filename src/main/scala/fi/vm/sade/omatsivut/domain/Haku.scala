package fi.vm.sade.omatsivut.domain

import org.joda.time.DateTime

case class Haku(name: Translations, applicationPeriods: List[HakuAika])
case class HakuAika(start: DateTime, end: DateTime)
