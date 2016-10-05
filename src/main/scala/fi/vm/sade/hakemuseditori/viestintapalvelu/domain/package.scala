package fi.vm.sade.hakemuseditori.viestintapalvelu

import org.joda.time.format.DateTimeFormat

case class Letter(val id: Int, val hakuOid: String, tyyppi: String, tiedostotyyppi: String, timestamp: String)

object LetterFormatter {
  val timestamp = DateTimeFormat.forPattern ("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

}
