package fi.vm.sade.hakemuseditori.ohjausparametrit.domain

case class HaunParametrit(haunAikataulu: Option[HaunAikataulu], jarjestetytHakutoiveet: Option[Boolean])
case class HaunAikataulu(julkistus: Option[TulostenJulkistus], hakukierrosPaattyy: Option[Long])
case class TulostenJulkistus(start: Long, end: Long)

