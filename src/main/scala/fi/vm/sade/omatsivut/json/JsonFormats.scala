package fi.vm.sade.omatsivut.json

import fi.vm.sade.omatsivut.tulokset.HakutoiveenValintatulosTila
import org.json4s._
import org.json4s.ext.{JodaTimeSerializers, EnumNameSerializer}

object JsonFormats {
  val genericFormats =  new DefaultFormats {
    override def dateFormatter = {
      val format = super.dateFormatter
      format.setTimeZone(DefaultFormats.UTC)
      format
    }
  } ++ JodaTimeSerializers.all
  val jsonFormats: Formats = JsonFormats.genericFormats ++ List(new QuestionNodeSerializer, new HakemusMuutosSerializer, new EnumNameSerializer(HakutoiveenValintatulosTila), new HakuSerializer, new KohteenHakuaikaSerializer)
}

trait JsonFormats {
  implicit val jsonFormats: Formats = JsonFormats.jsonFormats
}

