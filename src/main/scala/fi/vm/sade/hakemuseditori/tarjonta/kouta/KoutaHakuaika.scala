package fi.vm.sade.hakemuseditori.tarjonta.kouta

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter

import fi.vm.sade.hakemuseditori.tarjonta.domain.Hakuaika

case class KoutaHakuaika(alkaa: String,
                         paattyy: String) {
  def toHakuaika: Hakuaika = {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.of("Europe/Helsinki"))
    Hakuaika(
      id = "kouta-hakuaika-id",
      start = Instant.from(formatter.parse(alkaa)).toEpochMilli,
      end = Instant.from(formatter.parse(paattyy)).toEpochMilli
    )
  }
}

