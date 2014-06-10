package fi.vm.sade.omatsivut

import org.scalatra._
import javax.servlet.http.HttpServletRequest

trait OmatsivutStack extends ScalatraServlet {

  notFound {
    // remove content type in case it was set through an action
    contentType = null
    serveStaticResource() getOrElse resourceNotFound()
  }
}
