package fi.vm.sade.omatsivut.config

import fi.vm.sade.omatsivut.config.TempDbUtils.tryTimes

import fi.vm.sade.utils.slf4j.Logging
import fi.vm.sade.utils.tcp.PortChooser

import scala.sys.process.stringToProcess

class ITPostgres(portChooser: PortChooser) extends Logging {

  private def postgresAlreadyRunning(): Boolean = {
    val alreadyRunning = System.getProperty("omatsivut.it.postgres.alreadyrunning")
    (alreadyRunning != null && "true".equals(alreadyRunning))
  }

  val port = portChooser.chosenPort
  val dbName        = "omatsivut"
  val containerName = "omatsivut-postgres"

  val startStopRetries             = 100
  val startStopRetryIntervalMillis = 100

  def start() {
    if(!postgresAlreadyRunning) {
      try {
        if (!databaseIsRunning()) {
          startDatabaseContainer()
        }
      } finally {
        Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
          override def run() {
            stop()
          }
        }))
      }
    }
  }

  def stop() {
    if(!postgresAlreadyRunning) {
      try {
        logger.info("Killing PostgreSQL container")
        runBlocking(s"docker kill $containerName")
      } catch {
        case _: Throwable => logger.warn("PostgreSQL container didn't stop gracefully")
      }
    }
  }

  private val databaseIsRunning: () => Boolean = () => {
    runBlocking(
      s"docker exec $containerName pg_isready -q -t 1 -h localhost -U oph -d $dbName",
      failOnError = false
    ) == 0
  }

  def startDatabaseContainer(): Unit = {
    logger.info("Starting PostgreSQL container:")
    runBlocking(
      s"docker run --rm -d --name $containerName --env POSTGRES_PASSWORD=postgres -p $port:5432 omatsivut-postgres"
    )
    if (!tryTimes(startStopRetries, startStopRetryIntervalMillis)(databaseIsRunning)) {
      throw new RuntimeException(
        s"postgres not accepting connections in port $port after $startStopRetries attempts with $startStopRetryIntervalMillis ms intervals"
      )
    }
  }



  private def runBlocking(command: String, failOnError: Boolean = true): Int = {
    val returnValue = command.!
    if (failOnError && returnValue != 0) {
      throw new RuntimeException(s"Command '$command' exited with $returnValue")
    }
    returnValue
  }

}

object TempDbUtils {

  import scala.annotation.tailrec

  @tailrec
  def tryTimes(times: Int, sleep: Int)(thunk: () => Boolean): Boolean = times match {
    case n if n < 1 => false
    case 1          => thunk()
    case n =>
      thunk() || {
        Thread.sleep(sleep);
        tryTimes(n - 1, sleep)(thunk)
      }
  }
}

