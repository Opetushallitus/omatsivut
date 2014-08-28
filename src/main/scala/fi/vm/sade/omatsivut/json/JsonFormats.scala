package fi.vm.sade.omatsivut.json

import org.json4s._

object JsonFormats {
  val genericFormats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
}

trait JsonFormats {
  protected implicit val jsonFormats: Formats = JsonFormats.genericFormats ++ List(new QuestionNodeSerializer, new HakemusSerializer)
}



