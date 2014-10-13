package fi.vm.sade.omatsivut.ohjausparametrit

package object domain {

  case class Tulosaikataulu(julkistus: Option[Julkistus])
  case class Julkistus(start: Long, end: Long)

}
