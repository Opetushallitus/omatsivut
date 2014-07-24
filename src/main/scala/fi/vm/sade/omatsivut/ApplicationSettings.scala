package fi.vm.sade.omatsivut

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConversions._

object ApplicationSettings extends Logging {
  def loadSettings(fileLocations: List[String]): ApplicationSettings = {
    fileLocations.flatMap(getFile).headOption match {
      case Some(configFile) =>
        logger.info("Using configuration file " + configFile)
        val settings: Config = ConfigFactory.load(ConfigFactory.parseFile(configFile))
        val applicationSettings = new ApplicationSettings(settings)
        applicationSettings
      case None =>
        throw new RuntimeException("Configuration file missing. Please set the omatsivut.configFile property correctly, or make sure you have one of the following: " + fileLocations)
    }
  }

  private def getFile(name: String): List[File] = {
    if (new File(name).exists) {
      List(new File(name))
    } else {
      Nil
    }
  }

}
class ApplicationSettings(config: Config) {
  val casTicketUrl = config getString "omatsivut.cas.ticket.url"

  val authenticationServiceConfig = getRemoteApplicationConfig(config.getConfig("omatsivut.authentication-service"))

  val koulutusinformaatioAoUrl = config getString "omatsivut.koulutusinformaatio.ao.url"
  val koulutusinformaatioLopUrl = config getString "omatsivut.koulutusinformaatio.lop.url"

  val aesKey = config getString "omatsivut.crypto.aes.key"
  val hmacKey = config getString "omatsivut.crypto.hmac.key"

  private def getRemoteApplicationConfig(config: Config) = {
    RemoteApplicationConfig(config getString "url", config getString "username", config getString "password", config getString "path", config getString "ticket_consumer_path")
  }

  def toProperties = {
    val keys = config.entrySet().toList.map(_.getKey)
    keys.map { key =>
      (key, config.getString(key))
    }.toMap
  }
}