package fi.vm.sade.hakemuseditori.ohjausparametrit.domain

case class HaunAikataulu(julkistus: Option[TulostenJulkistus], hakukierrosPaattyy: Option[Long])

case class TulostenJulkistus(start: Long, end: Long)

