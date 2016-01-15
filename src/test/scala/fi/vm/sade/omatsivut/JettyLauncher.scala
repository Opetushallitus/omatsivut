package fi.vm.sade.omatsivut

import javax.net.ssl._

import fi.vm.sade.omatsivut.config.AppConfig
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object JettyLauncher {

  private def disableSSLHostNameVerification() {
    HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
      override def verify(hostname: String, session: SSLSession) = true
    })
  }

  def main(args: Array[String]) {
    disableSSLHostNameVerification()
    System.setProperty("omatsivut.port", System.getProperty("omatsivut.port", "7337"))
    new JettyLauncher().start.join()
  }
}

class JettyLauncher(profile: Option[String] = None) {
  private val javaVersion: String = System.getProperty("java.version")
  if (!javaVersion.startsWith("1.8")) {
    System.err.println(s"""------------------------------
                          |EXITING: Run omatsivut with Java 1.8, java.version was $javaVersion
                          |------------------------------""".stripMargin)
    System.exit(1)
  }
  val server = new Server(AppConfig.embeddedJettyPortChooser.chosenPort)
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

  val valintatulosservice = {
    System.setProperty("valintatulos.profile", "it-externalHakemus")
    System.setProperty("hakemus.embeddedmongo.port", AppConfig.embeddedMongoPortChooser.chosenPort.toString)
    val context = new WebAppContext()
    context.setContextPath("/valinta-tulos-service")
    context.setWar("target/valinta-tulos-service.war")
    context
  }
  handlers.addHandler(valintatulosservice)

  val valintarekisteri = {
    val context = new WebAppContext()
    context.setResourceBase("src/main/webapp")
    context.setContextPath("/valintarekisteri")
    context.setInitParameter(ScalatraListener.LifeCycleKey, "fi.vm.sade.omatsivut.valintarekisteri.MockedValintarekisteriScalatraBootstrap")
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")
    profile.foreach(context.setAttribute("valintarekisteri.profile", _))
    context
  }
  handlers.addHandler(valintarekisteri)

  server.setHandler(handlers)

  def start = {
    server.start()
    server
  }
}
