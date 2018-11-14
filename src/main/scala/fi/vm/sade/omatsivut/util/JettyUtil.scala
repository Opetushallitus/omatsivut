package fi.vm.sade.omatsivut.util

import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext

object JettyUtil extends LazyLogging {

  def createServerWithContext(portHttp: Int, portHttps: Option[Int], context: WebAppContext, dbUrl: String, user: String, password: String, secureSessionCookie: Boolean) = {
    val server = new Server()
    server.setHandler(context)
    server
  }

  // TODO: See JettyUtil.scala from hekuperusteet for ideas how to configure the postgres etc.

}
