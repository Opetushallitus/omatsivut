package fi.vm.sade.omatsivut

import com.mongodb.casbah.MongoClient
import com.mongodb.{MongoCredential, ServerAddress}
import com.typesafe.config.{ConfigFactory, Config}
import collection.JavaConversions._
import java.io.File

object ApplicationSettings extends Logging {
  def loadSettings(fileLocations: List[String]): ApplicationSettings = {
    fileLocations.flatMap(getFile).headOption match {
      case Some(configFile) =>
        logger.info("Using configuration file " + configFile)
        val config: Config = ConfigFactory.parseFile(configFile)
        val settings = ConfigFactory.load(config)
        val configReader = new ApplicationSettings(new SettingsReader() {
          def getProperty(key: String) = {
            settings.getString(key)
          }
          def keys = {
            settings.entrySet().toList.map(_.getKey)
          }
        })
        logger.info("Settings: " + settings)
        configReader
      case None =>
        throw new RuntimeException("Configuration file missing. Please set the omatsivut.configFile property correctly, or make sure you have ../module-install-parent or ~/oph-configuration/omatsivut.properties")
    }
  }

  private def getFile(name: String): List[File] = {
    if (name != null && new File(name).exists) {
      List(new File(name))
    } else {
      Nil
    }
  }

}
case class ApplicationSettings(settingsReader: SettingsReader) {
  val casTicketUrl = settingsReader getString "omatsivut.cas.ticket.url"

  val authenticationService = getRemoteApplicationConfig(settingsReader.getConfig("omatsivut.authentication-service"))

  val aesKey = settingsReader getString "omatsivut.crypto.aes.key"
  val hmacKey = settingsReader getString "omatsivut.crypto.hmac.key"

  private val hakuAppMongoHost = settingsReader getProperty "omatsivut.haku-app.mongo.host"
  private val hakuAppMongoPort = settingsReader getInt "omatsivut.haku-app.mongo.port"
  private val hakuAppMongoDbName = settingsReader getString "omatsivut.haku-app.mongo.db.name"
  private val hakuAppMongoDbUsername = settingsReader getString "omatsivut.haku-app.mongo.db.username"
  private val hakuAppMongoDbPassword = settingsReader getString "omatsivut.haku-app.mongo.db.password" toCharArray ()

  private def getRemoteApplicationConfig(config: SettingsReader) = {
    RemoteApplicationConfig(config getString "url", config getString "username", config getString "password", config getString "path", config getString "ticket_consumer_path")
  }

  private def hakuAppMongoClient: MongoClient = {
    val mongoAddress = new ServerAddress(hakuAppMongoHost, hakuAppMongoPort)
    if(hakuAppMongoDbUsername.isEmpty()) {
      MongoClient(mongoAddress)
    }
    else {
      MongoClient(
        List(mongoAddress),
        List(MongoCredential.createMongoCRCredential(hakuAppMongoDbUsername, hakuAppMongoDbName, hakuAppMongoDbPassword))
      )
    }
  }

  def hakuAppMongoDb = hakuAppMongoClient.getDB(hakuAppMongoDbName)

  override def toString = {
    "Mongo: " + hakuAppMongoHost + ":" + hakuAppMongoPort + "/" + hakuAppMongoDbName
  }
}


trait SettingsReader {
  def keys: Seq[String]
  def toMap: Map[String, String] = {
    keys.map { key =>
      (key, getString(key))
    }.toMap
  }
  def getProperty(key: String): String
  def getInt(key: String): Int = Integer.parseInt(getProperty(key))
  def getString(key: String) = getProperty(key)
  def getConfig(key: String): SettingsReader = {
    val parent = this
    new SettingsReader {
      def getProperty(subKey: String) = {
        parent.getProperty(key + "." + subKey)
      }
      def keys = throw new UnsupportedOperationException()
    }
  }
}