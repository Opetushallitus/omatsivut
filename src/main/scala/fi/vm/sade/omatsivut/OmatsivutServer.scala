package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.util.JettyUtil
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.util.resource.{Resource, ResourceCollection}
import org.eclipse.jetty.servlet.DefaultServlet
import org.scalatra.servlet.ScalatraListener
import org.slf4j.LoggerFactory

class OmatsivutServer {

  def portHttp = OphUrlProperties.require("omatsivut.port.http").toInt
  def portHttps = Option(OphUrlProperties.require("omatsivut.port.https").toInt)
  def secureSessionCookie = true

  def runServer() {
    val dbUrl = OphUrlProperties.getProperty("omatsivut.db.url")
    val user = OphUrlProperties.getProperty("omatsivut.db.user")
    val password = OphUrlProperties.getProperty("omatsivut.db.password")
    // OmatsivutDatabase(props)
    val context: WebAppContext = createContext
    val server = JettyUtil.createServerWithContext(portHttp, portHttps, context, dbUrl, user, password, secureSessionCookie)
    server.start()
    server.join()
    OmatsivutServer.logger.info(s"Using ports $portHttp and $portHttps")
  }

  def createContext = {
    val context = new WebAppContext()
    context.setBaseResource(Resource.newClassPathResource("/webapp"))
    context.setContextPath("/omatsivut")
    context.setInitParameter(ScalatraListener.LifeCycleKey, Class.forName("ScalatraBootstrap").getCanonicalName)
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
    logger.info("About to start " + s)
    s.runServer()
    logger.info("Started OmatsivutServer")
  }
}
