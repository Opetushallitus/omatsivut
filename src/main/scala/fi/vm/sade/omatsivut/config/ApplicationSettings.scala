package fi.vm.sade.omatsivut.config

import java.io.File

import com.typesafe.config.{ConfigValueFactory, Config, ConfigException, ConfigFactory}
import fi.vm.sade.omatsivut.util.Logging
import fi.vm.sade.security.cas.{CasTicketRequest, CasConfig}
import fi.vm.sade.security.ldap.LdapConfig
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
  def withOverride(keyValuePair : (String, String)) = {
    ApplicationSettings(config.withValue(keyValuePair._1, ConfigValueFactory.fromAnyRef(keyValuePair._2)))
  }

  val casTicketUrl = config.getString("omatsivut.cas.ticket.url")

  val raamitUrl = config.getString("omatsivut.oppija-raamit.url")

  val piwikUrl = config.getString("omatsivut.piwik.url")

  val securitySettings = new SecuritySettings(config)
  val authenticationServiceConfig = getRemoteApplicationConfig(config.getConfig("omatsivut.authentication-service"))

  val valintaTulosServiceUrl = config.getString("omatsivut.valinta-tulos-service.url")
  val koulutusinformaatioAoUrl = config.getString("omatsivut.koulutusinformaatio.ao.url")
  val koulutusinformaatioLopUrl = config.getString("omatsivut.koulutusinformaatio.lop.url")

  val ohjausparametritUrl = config.getString("omatsivut.ohjausparametrit.url")
  val tarjontaUrl = config.getString("omatsivut.tarjonta.url")
  val koodistoUrl = config.getString("cas.service.koodisto-service")

  val aesKey = config.getString("omatsivut.crypto.aes.key")
  val hmacKey = config.getString("omatsivut.crypto.hmac.key")

  val environment = Environment(getStringWithDefault("environment", "default"))

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

case class Environment(val name: String) {
  def isLuokka = name == "ophitest"
  def isReppu = name == "oph"
  def isProduction = name == "ophprod"
  def isQA = name == "ophp"
}

class SecuritySettings(c: Config) {
  val casConfig = CasConfig(c.getString("cas.url"))
  val casUsername = c.getString("cas.username")
  val casPassword = c.getString("cas.password")
}