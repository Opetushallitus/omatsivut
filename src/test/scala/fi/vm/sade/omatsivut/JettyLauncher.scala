package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.config.AppConfig
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.webapp.WebAppContext

object JettyLauncher {
  def main(args: Array[String]) {
    new JettyLauncher(AppConfig.embeddedJettyPort).start.join
  }
}

class JettyLauncher(val port: Int, profile: Option[String] = None) {
  val useEmbeddedVts = !System.getProperty("java.version").startsWith("1.7")
  val server = new Server(port)
  val handlers = new HandlerCollection()

  val omatsivut = {
    val context = new WebAppContext()
    context.setResourceBase("src/main/webapp")
    context.setContextPath("/omatsivut")
    context.setDescriptor("src/main/webapp/WEB-INF/web.xml")
    profile.foreach(context.setAttribute("omatsivut.profile", _))
    context
  }
  handlers.addHandler(omatsivut)

  if (useEmbeddedVts) {
    val valintatulosservice = {
      System.setProperty("valintatulos.profile", "it-externalHakemus")
      System.setProperty("hakemus.embeddedmongo.port", AppConfig.embeddedmongoPortChooser.chosenPort.toString)
      val context = new WebAppContext();
      context.setContextPath("/valinta-tulos-service");
      context.setWar("target/valinta-tulos-service.war");
      context
    }
    handlers.addHandler(valintatulosservice)
    ValintatulosServiceRunner.runner = new ValintatulosServiceRunner {
      def port = JettyLauncher.this.port
      def start = {}
    }
  }

  server.setHandler(handlers)

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