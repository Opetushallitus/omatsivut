package fi.vm.sade.omatsivut

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{DefaultServlet, ServletContextHandler}
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object JettyLauncher {
  def main(args: Array[String]) {
    new JettyLauncher(System.getProperty("omatsivut.port", "8080").toInt).start.join
  }
}

class JettyLauncher(port: Int) {
  val server = new Server(port)
  val context = new WebAppContext()
  context.setResourceBase("src/main/webapp")
  context.setContextPath("/omatsivut")
  context.setDescriptor("src/main/webapp/WEB-INF/web.xml")
  server.setHandler(context)

  def start = {
    server.start
    server
  }


  def withJetty[T](block: => T) = {
    val server = start
    try {
      block
    } finally {
      server.stop
    }
  }
}