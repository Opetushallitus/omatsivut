package fi.vm.sade.hakemuseditori.ohjausparametrit

package object domain {
  case class HaunAikataulu(julkistus: Option[TulostenJulkistus], hakukierrosPaattyy: Option[Long])
  case class TulostenJulkistus(start: Long, end: Long)
}
