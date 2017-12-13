package fi.vm.sade.hakemuseditori.http

import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.utils.Timer._
import fi.vm.sade.utils.http.DefaultHttpClient
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods._

trait HttpCall extends JsonFormats {

  def withHttpGet[T](timedDesc: String, url: String, block: Option[JValue] => Option[T]): Option[T] = {
    timed(timedDesc, 1000) {
      val (responseCode, _, resultString) =
        DefaultHttpClient.httpGet(url).responseWithHeaders()
      responseCode match {
        case 200 =>
          val parsed = parse(resultString, useBigDecimalForDouble = false).extractOpt[JValue]
          block(parsed)
        case _ => None
      }
    }
  }
}
