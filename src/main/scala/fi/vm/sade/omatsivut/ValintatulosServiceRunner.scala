package fi.vm.sade.omatsivut

import java.nio.file.{Files, Paths}

import fi.vm.sade.omatsivut.mongo.EmbeddedMongo
import fi.vm.sade.omatsivut.util.{Logging, PortChecker}

object ValintatulosServiceRunner extends Logging {
  import scala.sys.process._

  var valintatulosPort = sys.props.getOrElse("valintatulos.port", PortChecker.findFreeLocalPort.toString).toInt

  val searchPaths = List("./valinta-tulos-service", "../valinta-tulos-service")
  var currentRunner: Option[scala.sys.process.Process] = None

  def start = this.synchronized {
    if (currentRunner == None && PortChecker.isFreeLocalPort(valintatulosPort)) {
      findValintatulosService match {
        case Some(path) => {
          logger.info("Starting valinta-tulos-service from " + path + " on port "+ valintatulosPort)
          val cwd = new java.io.File(path)
          val javaHome = System.getProperty("JAVA8_HOME", "")
          Process(List("./sbt", "-no-colors", "test:compile"), cwd, "JAVA_HOME" -> javaHome).!
          val process = Process(List("./sbt", "-no-colors", "test:run-main fi.vm.sade.valintatulosservice.JettyLauncher", "-Dvalintatulos.port=" + valintatulosPort, "-Dvalintatulos.profile=it-externalHakemus", "-Dhakemus.embeddedmongo.port=" + EmbeddedMongo.port,  "-Dfile.encoding=UTF-8"), cwd, "JAVA_HOME" -> javaHome).run(true)
          for (i <- 0 to 60 if PortChecker.isFreeLocalPort(valintatulosPort)) {
            Thread.sleep(1000)
          }
          currentRunner = Some(process)
          sys.addShutdownHook { ValintatulosServiceRunner.stop }
        }
        case _ =>
          logger.error("******* valinta-tulos-service not found ********")
      }
    } else {
      logger.info("Not starting valinta-tulos-service: seems to be running on port " + valintatulosPort)
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