package fi.vm.sade.omatsivut.util

import java.security.MessageDigest

import ch.qos.logback.access.jetty.RequestLogImpl
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jetty.server._
import org.eclipse.jetty.server.session._
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.eclipse.jetty.webapp.WebAppContext

object JettyUtil extends LazyLogging {

  def createServerWithContext(portHttp: Int, portHttps: Option[Int], context: WebAppContext, dbUrl: String, user: String, password: String, secureSessionCookie: Boolean) = {
    val server = new Server()
    server.setHandler(context)
    server.setConnectors(createConnectors(portHttp, portHttps, server))
    initRequestLog(server)
    if(secureSessionCookie) {
      setSecureCookieParams(context)
    }
    server
  }

  def initRequestLog(server: Server): Unit = {
    val requestLog = new RequestLogImpl()
    requestLog.setFileName(sys.props.getOrElse("logback.access", "src/main/resources/logback-access.xml"))
    server.setRequestLog(requestLog)
    requestLog.start()
  }

  private def createConnectors(portHttp: Int, portHttps: Option[Int], server: Server): Array[Connector] = {
    Array(createHttpConnector(portHttp, server))  ++
      portHttps.map(p => Array(createSSLConnector(p,server))).getOrElse(Array())
  }

  private def createHttpConnector(portHttp: Int, server: Server): Connector = {
    val httpConnector = new ServerConnector(server, new HttpConnectionFactory(new HttpConfiguration))
    httpConnector.setPort(portHttp)
    httpConnector
  }

  private def createSSLConnector(port: Int, server: Server): Connector = {
    val sslContextFactory = new SslContextFactory
    sslContextFactory.setKeyStoreType("jks")
    sslContextFactory.setKeyStorePath(this.getClass.getClassLoader.getResource("keystore").toExternalForm)
    sslContextFactory.setKeyStorePassword("keystore")
    sslContextFactory.setKeyManagerPassword("keystore")

    val httpsConfig = new HttpConfiguration
    httpsConfig.setSecurePort(port)
    httpsConfig.addCustomizer(new SecureRequestCustomizer)

    val https = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(httpsConfig))
    https.setPort(port)
    https
  }

  private def createWorkerName = {
    val serverName = java.net.InetAddress.getLocalHost.getHostName.split("\\.")(0)
    MessageDigest.getInstance("MD5").digest(serverName.getBytes).map("%02x".format(_)).mkString.slice(0, 10)
  }

  def setSecureCookieParams(context: WebAppContext) {
    val sessionCookieConfig = context.getServletContext.getSessionCookieConfig
    sessionCookieConfig.setHttpOnly(true)
    sessionCookieConfig.setSecure(true)
  }
}
