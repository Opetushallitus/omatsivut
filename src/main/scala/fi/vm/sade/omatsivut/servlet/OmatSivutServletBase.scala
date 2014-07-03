package fi.vm.sade.omatsivut.servlet

import org.scalatra._
import org.slf4j.LoggerFactory
import fi.vm.sade.omatsivut.Logging

trait OmatSivutServletBase extends ScalatraServlet with Logging {
  notFound {
    // remove content type in case it was set through an action
    contentType = null
    serveStaticResource() getOrElse resourceNotFound()
  }

  error {
    case e => {
      logger.error(request.getMethod + " " + requestPath, e);
      response.setStatus(500)
      "500 Internal Server Error"
    }
  }
}
