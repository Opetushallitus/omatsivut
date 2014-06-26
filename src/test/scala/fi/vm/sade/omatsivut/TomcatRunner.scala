package fi.vm.sade.omatsivut

import org.apache.catalina.startup.Tomcat
import java.io.File

/**
 * Runs the application in an embedded Tomcat. Suitable for running in IDEA or Eclipse.
 */
object TomcatRunner extends App {
  val tomcat = new Tomcat()
  val webappDirLocation = "src/main/webapp/"
  tomcat.setPort(8080)
  tomcat.addWebapp("/omatsivut", new File(webappDirLocation).getAbsolutePath());
  System.out.println("configuring app with basedir: " + new File("./" + webappDirLocation).getAbsolutePath());

  tomcat.start();
  tomcat.getServer().await();
}
