package fi.vm.sade.omatsivut

import java.nio.file.{Files, Paths}

import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.utils.slf4j.Logging
import fi.vm.sade.utils.tcp.{PortChecker, PortFromSystemPropertyOrFindFree}

object ValintatulosServiceRunner extends Logging {
  import scala.sys.process._

  val valintatulosPortChooser = new PortFromSystemPropertyOrFindFree("valintatulos.port")

  val searchPaths = List("./valinta-tulos-service", "../valinta-tulos-service")
  var currentRunner: Option[scala.sys.process.Process] = None

  def start = this.synchronized {
    if (currentRunner == None && PortChecker.isFreeLocalPort(valintatulosPortChooser.chosenPort)) {
      findValintatulosService match {
        case Some(path) => {
          logger.info("Starting valinta-tulos-service from " + path + " on port "+ valintatulosPortChooser.chosenPort)
          val cwd = new java.io.File(path)
          var javaHome = System.getProperty("JAVA8_HOME", "")
          if (javaHome.contains("{")) {
            javaHome ="";
          }

          var mvn = System.getProperty("mvn", "mvn");

          logger.info("Using java home:" + javaHome);

          val process = Process(List(mvn, "tomcat7:run", "-Dmaven.tomcat.port=" + valintatulosPortChooser.chosenPort, "-Dvalintatulos.profile=it-externalHakemus", "-Dhakemus.embeddedmongo.port=" + AppConfig.embeddedmongoPortChooser.chosenPort,  "-Dfile.encoding=UTF-8"), cwd, "JAVA_HOME" -> javaHome).run(true)
          for (i <- 0 to 60 if PortChecker.isFreeLocalPort(valintatulosPortChooser.chosenPort)) {
            Thread.sleep(1000)
          }
          currentRunner = Some(process)
          sys.addShutdownHook { ValintatulosServiceRunner.stop }
        }
        case _ =>
          logger.error("******* valinta-tulos-service not found ********")
      }
    } else {
      logger.info("Not starting valinta-tulos-service: seems to be running on port " + valintatulosPortChooser.chosenPort)
    }
  }

  def stop = this.synchronized {
    logger.info("Stoping valinta-tulos-service")
    currentRunner.foreach(_.destroy)
  }

  private def findValintatulosService = {
    searchPaths.find((path) => Files.exists(Paths.get(path)))
  }
}