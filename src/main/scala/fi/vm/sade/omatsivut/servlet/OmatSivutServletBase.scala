package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.Logging
import org.scalatra._
import fi.vm.sade.omatsivut.domain.Language

trait OmatSivutServletBase extends ScalatraServlet with Logging {

  implicit def language: Language.Language = {
    Option(request.getAttribute("lang").asInstanceOf[Language.Language]).getOrElse(Language.fi)
  }

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

  protected def headerOption(name: String): Option[String] = {
    Option(request.getHeader(name))
  }

  protected def paramOption(name: String): Option[String] = {
    try {
      Option(params(name))
    } catch {
      case e: Exception => None
    }
  }
}
