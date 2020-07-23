package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.utils.slf4j.Logging
import org.scalatra.ScalatraFilter

trait OmatSivutFilterBase extends ScalatraFilter with Logging {

  error {
    case e => {
      logger.error(request.getMethod + " " + requestPath, e)
      response.setStatus(500)
      "500 Internal Server Error"
    }
  }
}
