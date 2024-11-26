package fi.vm.sade.omatsivut.util

import java.io.File

import com.typesafe.config.{Config, ConfigException, ConfigFactory, ConfigValueFactory}

object ApplicationSettingsLoader extends Logging {
  def loadSettings[T <: ApplicationSettings](fileLocation: String)(implicit parser: ApplicationSettingsParser[T]): T = {
    val configFile = new File(fileLocation)
    if (configFile.exists()) {
      logger.info("Using configuration file " + configFile)
      parser.parse(ConfigFactory.load(ConfigFactory.parseFile(configFile)))
    } else {
      throw new RuntimeException("Configuration file not found: " + fileLocation)
    }
  }
}

abstract class ApplicationSettings(config: Config) {

  val environment = Environment(getStringWithDefault("environment", "default"))

  def toProperties = {
    import scala.collection.JavaConversions._

    val keys = config.entrySet().toList.map(_.getKey)
    keys.map { key =>
      (key, config.getString(key))
    }.toMap
  }

  def withOverride[T <: ApplicationSettings](keyValuePair : (String, String))(implicit parser: ApplicationSettingsParser[T]): T = {
    parser.parse(config.withValue(keyValuePair._1, ConfigValueFactory.fromAnyRef(keyValuePair._2)))
  }

  def withoutPath[T <: ApplicationSettings](path: String)(implicit  parser: ApplicationSettingsParser[T]): T = {
    parser.parse(config.withoutPath(path))
  }

  protected def getMongoConfig(config: Config) = {
    MongoConfig(
      config.getString("uri"),
      config.getString("dbname")
    )
  }

  def getStringWithDefault(path: String, default: String) = {
    try {
      config.getString(path)
    } catch {
      case _ :ConfigException.Missing | _ :ConfigException.Null | _ : ConfigException.WrongType => default
    }
  }
}

trait ApplicationSettingsParser[T <: ApplicationSettings] {

  def parse(config: Config): T

}

case class MongoConfig(url: String, dbname: String)

case class Environment(val name: String) {
  def isLuokka = name == "ophitest"
  def isVagrant = name == "vagrant"
  def isReppu = name == "oph"
  def isProduction = name == "ophprod"
  def isQA = name == "ophp"
  def isKoulutus = name == "ophtrain"
  def isDev = name == "dev"
}

