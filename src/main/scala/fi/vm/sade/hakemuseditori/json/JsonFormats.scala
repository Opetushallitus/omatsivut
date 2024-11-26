package fi.vm.sade.hakemuseditori.json

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.valintatulokset.domain.VastaanottoActionSerializer
import fi.vm.sade.omatsivut.util.GenericJsonFormats
import org.json4s._
import org.json4s.ext.EnumNameSerializer

object JsonFormats {
  val jsonFormats: Formats = GenericJsonFormats.genericFormats ++ List(
    new ApplicationSerializer,
    new HakuSerializer,
    new KohteenHakuaikaSerializer,
    new EnumNameSerializer(Language),
    new VastaanottoActionSerializer
  )
}

trait JsonFormats {
  implicit val jsonFormats: Formats = JsonFormats.jsonFormats
}

