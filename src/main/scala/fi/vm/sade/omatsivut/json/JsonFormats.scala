package fi.vm.sade.omatsivut.json

import fi.vm.sade.omatsivut.hakemus.domain._
import org.json4s._
import org.json4s.ext.EnumNameSerializer

object JsonFormats {
  val genericFormats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
  val jsonFormats: Formats = JsonFormats.genericFormats ++ List(new QuestionNodeSerializer, new HakemusMuutosSerializer, new EnumNameSerializer(HakutoiveenValintatulosTila))
}

trait JsonFormats {
  implicit val jsonFormats: Formats = JsonFormats.jsonFormats
}

