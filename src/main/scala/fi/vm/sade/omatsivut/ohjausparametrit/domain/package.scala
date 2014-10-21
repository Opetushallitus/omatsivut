package fi.vm.sade.omatsivut.ohjausparametrit

package object domain {

  case class HaunAikataulu(julkistus: Option[Julkistus], hakukierrosPaattyy: Option[Long])
  case class Julkistus(start: Long, end: Long)

}
