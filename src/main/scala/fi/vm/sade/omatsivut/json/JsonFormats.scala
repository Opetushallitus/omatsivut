package fi.vm.sade.omatsivut.json

import fi.vm.sade.omatsivut.hakemus.domain.HakutoiveenValintatulosTila.HakutoiveenValintatulosTila
import fi.vm.sade.omatsivut.hakemus.domain._
import org.json4s._

object JsonFormats {
  val genericFormats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
  val jsonFormats: Formats = JsonFormats.genericFormats ++ List(new QuestionNodeSerializer, new HakemusMuutosSerializer, new org.json4s.ext.EnumNameSerializer(HakutoiveenValintatulosTila))
}

trait JsonFormats {
  implicit val jsonFormats: Formats = JsonFormats.jsonFormats
}

