package fi.vm.sade.omatsivut

import java.nio.file.{Paths, Files}

import fi.vm.sade.omatsivut.util.{Logging, PortChecker}

object ValintatulosServiceRunner extends Logging {
  import sys.process._
  val valintatulosPort = 8097
  val searchPaths = List("./valinta-tulos-service", "../valinta-tulos-service")
  var currentRunner: Option[scala.sys.process.Process] = None

  def start = this.synchronized {
    if (currentRunner == None && PortChecker.isFreeLocalPort(valintatulosPort)) {
      findValintatulosService match {
        case Some(path) => {
          logger.info("Starting valinta-tulos-service from " + path)
          val cwd = new java.io.File(path)
          val javaHome = System.getProperty("JAVA8_HOME", "")
          Process(List("./sbt", "test:compile"), cwd, "JAVA_HOME" -> javaHome).!
          val process = Process(List("./sbt", "test:run-main fi.vm.sade.valintatulosservice.JettyLauncher", "-Dvalintatulos.profile=it-externalHakemus", "-Dfile.encoding=UTF-8"), cwd, "JAVA_HOME" -> javaHome).run(true)
          for (i <- 0 to 60 if PortChecker.isFreeLocalPort(valintatulosPort)) {
            Thread.sleep(1000)
          }
          currentRunner = Some(process)
        }
        case _ =>
          logger.error("******* valinta-tulos-service not found ********")
      }
    }
  }

  def stop = this.synchronized {
    currentRunner.foreach(_.destroy)
  }

  def withValintatulosService[T](block: => T) = {
    start
    block
  }

  private def findValintatulosService = {
    searchPaths.find((path) => Files.exists(Paths.get(path)))
  }
}