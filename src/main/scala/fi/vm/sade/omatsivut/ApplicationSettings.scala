package fi.vm.sade.omatsivut

import java.io.File

import com.typesafe.config.{ConfigException, Config, ConfigFactory}

import scala.collection.JavaConversions._

object ApplicationSettings extends Logging {
  def loadSettings(fileLocation: String): ApplicationSettings = {
    val configFile = new File(fileLocation)
    if (configFile.exists()) {
      logger.info("Using configuration file " + configFile)
      val settings: Config = ConfigFactory.load(ConfigFactory.parseFile(configFile))
      val applicationSettings = new ApplicationSettings(settings)
      applicationSettings
    } else {
      throw new RuntimeException("Configuration file not found: " + fileLocation)
    }
  }
}
case class ApplicationSettings(config: Config) {
  val casTicketUrl = config.getString("omatsivut.cas.ticket.url")

  val raamitUrl = config.getString("omatsivut.oppija-raamit.url")

  val authenticationServiceConfig = getRemoteApplicationConfig(config.getConfig("omatsivut.authentication-service"))

  val koulutusinformaatioAoUrl = config.getString("omatsivut.koulutusinformaatio.ao.url")
  val koulutusinformaatioLopUrl = config.getString("omatsivut.koulutusinformaatio.lop.url")

  val ohjausparametritUrl = config.getString("omatsivut.ohjausparametrit.url")

  val aesKey = config.getString("omatsivut.crypto.aes.key")
  val hmacKey = config.getString("omatsivut.crypto.hmac.key")

  val environment = getStringWithDefault("environment", "default")

  def getStringWithDefault(path: String, default: String) = {
    try {
      config.getString(path)
    } catch {
      case _ :ConfigException.Missing | _ :ConfigException.Null => default
    }
  }

  private def getRemoteApplicationConfig(config: Config) = {
    RemoteApplicationConfig(
      config.getString("url"),
      config.getString("username"),
      config.getString("password"),
      config.getString("ticket_consumer_path"),
      config
    )
  }

  def toProperties = {
    val keys = config.entrySet().toList.map(_.getKey)
    keys.map { key =>
      (key, config.getString(key))
    }.toMap
  }
}