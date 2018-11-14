package fi.vm.sade.omatsivut

import fi.vm.sade.ScalatraBootstrap
import fi.vm.sade.omatsivut.util.JettyUtil
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.util.resource.ResourceCollection
import org.eclipse.jetty.servlet.DefaultServlet
import org.scalatra.servlet.ScalatraListener
import org.slf4j.LoggerFactory

class OmatsivutServer {

  def portHttp = 3 // props.getInt("hakuperusteet.port.http")
  def portHttps = Option(4) // props.getInt("hakuperusteet.port.https")).find(_ != -1)
  def secureSessionCookie = true

  def runServer() {
    val dbUrl = "abc"
    val user = "asdf"
    val password = "sfdsf"
    val context: WebAppContext = createContext
    val server = JettyUtil.createServerWithContext(portHttp, portHttps, context, dbUrl, user, password, secureSessionCookie)
    server.start()
    server.join()
    OmatsivutServer.logger.info(s"Using ports $portHttp and $portHttps")
  }

  def createContext = {
    val context = new WebAppContext()
    val resources = new ResourceCollection(Array(
      getClass.getClassLoader.getResource("webapp").toExternalForm
    ))
    context.setContextPath("/hakuperusteet-changeme/")
    context.setBaseResource(resources)
    context.setInitParameter(ScalatraListener.LifeCycleKey, classOf[ScalatraBootstrap].getCanonicalName)
    context.setInitParameter(org.scalatra.EnvironmentKey, "production")
    context.setInitParameter(org.scalatra.CorsSupport.EnableKey, "false")
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")
    context
  }
}


object OmatsivutServer {
  val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    val s = new OmatsivutServer
    s.runServer()
    logger.info("Started OmatsivutServer")
  }
}
