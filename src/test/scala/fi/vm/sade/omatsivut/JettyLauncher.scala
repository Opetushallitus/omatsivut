package fi.vm.sade.omatsivut

import javax.net.ssl._
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.util.PortChecker
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.webapp.WebAppContext

object JettyLauncher {

  private def disableSSLHostNameVerification() {
    HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
      override def verify(hostname: String, session: SSLSession) = true
    })
  }

  def main(args: Array[String]) {
    disableSSLHostNameVerification()
    System.setProperty("omatsivut.port", System.getProperty("omatsivut.port", "7337"))
    System.setProperty("omatsivut.it.postgres.port", PortChecker.findFreeLocalPort.toString)
    new JettyLauncher().start.join()
  }
}

class JettyLauncher(profile: Option[String] = None) {
  private val javaVersion: String = System.getProperty("java.version")
  if (!javaVersion.startsWith("17")) {
    System.err.println(s"""------------------------------
                          |EXITING: Run omatsivut with Java 17, java.version was $javaVersion
                          |------------------------------""".stripMargin)
    System.exit(1)
  }
  System.setProperty("fi.vm.sade.javautils.http.HttpServletRequestUtils.SKIP_MISSING_HEADER_LOGGING", "true")
  val server = new Server(AppConfig.embeddedJettyPortChooser.chosenPort)
  val handlers = new HandlerCollection()

  val omatsivut: WebAppContext = {
    val context = new WebAppContext()
    context.setResourceBase("src/main/webapp")
    context.setContextPath("/omatsivut")
    context.setDescriptor("src/main/webapp/WEB-INF/web.xml")
    profile.foreach(context.setAttribute("omatsivut.profile", _))
    context
  }
  handlers.addHandler(omatsivut)


  server.setHandler(handlers)

  def start: Server = {
    server.start()
    server
  }
}
