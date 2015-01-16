package fi.vm.sade.omatsivut.json

import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.tulokset.HakutoiveenValintatulosTila
import fi.vm.sade.utils.json4s.GenericJsonFormats
import org.json4s._
import org.json4s.ext.EnumNameSerializer

object JsonFormats {
  val jsonFormats: Formats = GenericJsonFormats.genericFormats ++ List(
    new QuestionNodeSerializer,
    new HakemusMuutosSerializer,
    new EnumNameSerializer(HakutoiveenValintatulosTila),
    new HakuSerializer,
    new KohteenHakuaikaSerializer,
    new EnumNameSerializer(Language)
  )
}

trait JsonFormats {
  implicit val jsonFormats: Formats = JsonFormats.jsonFormats
}

