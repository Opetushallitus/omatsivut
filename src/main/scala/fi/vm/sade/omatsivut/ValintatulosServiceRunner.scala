package fi.vm.sade.omatsivut

import java.nio.file.{Files, Paths}
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.utils.slf4j.Logging
import fi.vm.sade.utils.tcp.{PortChecker, PortFromSystemPropertyOrFindFree}
import scala.sys.process.Process

object ValintatulosServiceRunner extends Logging {
  var runner: ValintatulosServiceRunner = new MavenValintatulosServiceRunner
}

trait ValintatulosServiceRunner {
  def port: Int
  def start
}

class MavenValintatulosServiceRunner extends ValintatulosServiceRunner with Logging {
  val port = new PortFromSystemPropertyOrFindFree("valintatulos.port").chosenPort
  var currentRunner: Option[scala.sys.process.Process] = None

  def start = this.synchronized {
    if (currentRunner == None && PortChecker.isFreeLocalPort(port)) {
      findValintatulosService match {
        case Some(path) => {
          logger.info("Starting valinta-tulos-service from " + path + " on port "+ port)
          val cwd = new java.io.File(path)
          var envProperties = getEnvProperties();
          val mvn = System.getProperty("mvn", "mvn");

          val process = Process(List(mvn, "test-compile", "exec:java", "-Dvalintatulos.port=" + port, "-Dvalintatulos.profile=it-externalHakemus", "-Dhakemus.embeddedmongo.port=" + AppConfig.embeddedmongoPortChooser.chosenPort,  "-Dfile.encoding=UTF-8"), cwd, envProperties: _*).run(true)
          for (i <- 0 to 60 if PortChecker.isFreeLocalPort(port)) {
            Thread.sleep(1000)
          }
          currentRunner = Some(process)
          sys.addShutdownHook {
            this.synchronized {
              logger.info("Stoping valinta-tulos-service")
              currentRunner.foreach(_.destroy)
            }
          }
        }
        case _ =>
          logger.error("******* valinta-tulos-service not found ********")
      }
    } else {
      logger.info("Not starting valinta-tulos-service: seems to be running on port " + port)
    }
  }

  private val searchPaths = List("./valinta-tulos-service", "../valinta-tulos-service")

  private def getEnvProperties(): Seq[(String, String)] = {
    var javaHome = System.getProperty("JAVA8_HOME", System.getenv("JAVA8_HOME"))
    if (javaHome == null || javaHome.contains("{")) {
      javaHome = "";
    }
    if (javaHome.isEmpty) {
      throw new IllegalStateException("No JAVA8_HOME system property or environment variable set");
    }
    logger.info("Using java home:" + javaHome);
    List("JAVA_HOME" -> javaHome)
  }

  private def findValintatulosService = {
    searchPaths.find((path) => Files.exists(Paths.get(path)))
  }
}