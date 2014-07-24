package fi.vm.sade.omatsivut

import java.io.File

import org.apache.catalina.startup.Tomcat

/**
 * Runs the application in an embedded Tomcat. Suitable for running in IDEA or Eclipse.
 */
object TomcatRunner extends App {
  val tomcat = new Tomcat()
  val webappDirLocation = "src/main/webapp/"

  tomcat.setPort(8080)
  tomcat.addWebapp("/omatsivut", new File(webappDirLocation).getAbsolutePath())

  tomcat.start()
  tomcat.getServer().await()
}
