package fi.vm.sade.hakemuseditori.tarjonta.kouta

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter

import fi.vm.sade.hakemuseditori.tarjonta.domain.{Hakuaika, KohteenHakuaika}

import scala.util.Try

case class KoutaHakuaika(alkaa: String,
                         paattyy: Option[String]) {

  val defaultPaattymisaika = Try{ 1924952400000L } // 31.12.2030 klo 15:00

  def toHakuaika: Try[Hakuaika] = {
    for {
      start <- convertToMillis(alkaa)
      end <- paattyy.map(p => convertToMillis(p)).getOrElse(defaultPaattymisaika)
    } yield Hakuaika(id = "kouta-hakuaika-id", start, end)
  }

  def toKohteenHakuaika: Try[KohteenHakuaika] = {
    for {
      start <- convertToMillis(alkaa)
      end <- paattyy.map(p => convertToMillis(p)).getOrElse(defaultPaattymisaika)
    } yield KohteenHakuaika(start, end)
  }

  private def convertToMillis(datetime: String): Try[Long] = Try {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.of("Europe/Helsinki"))
    Instant.from(formatter.parse(datetime)).toEpochMilli
  }
}
