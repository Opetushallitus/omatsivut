package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.config.AppConfig
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext

object JettyLauncher {
  def main(args: Array[String]) {
    new JettyLauncher(AppConfig.embeddedJettyPort).start.join
  }
}

class JettyLauncher(val port: Int, profile: Option[String] = None) {
  val server = new Server(port)
  val context = new WebAppContext()
  context.setResourceBase("src/main/webapp")
  context.setContextPath("/omatsivut")
  context.setDescriptor("src/main/webapp/WEB-INF/web.xml")
  profile.foreach (context.setAttribute("omatsivut.profile", _))
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