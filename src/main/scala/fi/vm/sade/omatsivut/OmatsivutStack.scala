package fi.vm.sade.omatsivut

import org.scalatra._
import org.slf4j.LoggerFactory

trait OmatsivutStack extends ScalatraServlet {

  val logger = LoggerFactory.getLogger(getClass)

  notFound {
    // remove content type in case it was set through an action
    contentType = null
    serveStaticResource() getOrElse resourceNotFound()
  }
}
