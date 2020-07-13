package fi.vm.sade.hakemuseditori.http

import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.utils.Timer._
import fi.vm.sade.utils.http.DefaultHttpClient
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods._

trait HttpCall extends JsonFormats with Logging {

  def withHttpGet[T](timedDesc: String, url: String, block: Option[JValue] => Option[T]): Option[T] = {
    timed(timedDesc, 1000) {
      val (responseCode, _, resultString) =
        DefaultHttpClient.httpGet(url)(AppConfig.callerId).header("Caller-Id", AppConfig.callerId).responseWithHeaders()
      responseCode match {
        case 200 =>
          val parsed = parse(resultString, useBigDecimalForDouble = false).extractOpt[JValue]
          block(parsed)
        case errorCode =>
          logger.error(s"Response code ${errorCode} from url ${url}, expected: 200. Content: ${resultString}")
          None
      }
    }
  }
}
