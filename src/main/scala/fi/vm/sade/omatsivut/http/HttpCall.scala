package fi.vm.sade.omatsivut.http

import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.util.Timer._
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods._

trait HttpCall extends JsonFormats {

  def withHttpGet[T](timedDesc: String, url: String, block: Option[JValue] => Option[T]): Option[T] = {
    timed(1000, timedDesc) {
      val (responseCode, _, resultString) =
        DefaultHttpClient.httpGet(url).responseWithHeaders()
      responseCode match {
        case 200 =>
          val parsed = parse(resultString).extractOpt[JValue]
          block(parsed)
        case _ => None
      }
    }
  }
}
