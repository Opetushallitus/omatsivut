package fi.vm.sade.hakemuseditori.tarjonta.kouta

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter

import fi.vm.sade.hakemuseditori.tarjonta.domain.{Hakuaika, KohteenHakuaika}

case class KoutaHakuaika(alkaa: String,
                         paattyy: String) {
  def toHakuaika: Hakuaika = {
    Hakuaika(
      id = "kouta-hakuaika-id",
      start = convertToMillis(alkaa),
      end = convertToMillis(paattyy)
    )
  }

  def toKohteenHakuaika: KohteenHakuaika = {
    KohteenHakuaika(
      start = convertToMillis(alkaa),
      end = convertToMillis(paattyy)
    )
  }

  private def convertToMillis(datetime: String): Long = {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.of("Europe/Helsinki"))
    Instant.from(formatter.parse(datetime)).toEpochMilli
  }
}
