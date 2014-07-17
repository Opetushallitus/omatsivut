package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.Logging
import org.scalatra._

trait OmatSivutServletBase extends ScalatraServlet with Logging {
  notFound {
    // remove content type in case it was set through an action
    contentType = null
    serveStaticResource() getOrElse resourceNotFound()
  }

  error {
    case e => {
      logger.error(request.getMethod + " " + requestPath, e);
      e.printStackTrace()
      response.setStatus(500)
      "500 Internal Server Error"
    }
  }
}
