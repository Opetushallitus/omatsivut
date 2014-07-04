package fi.vm.sade.omatsivut

import com.mongodb.casbah.MongoClient
import com.mongodb.{MongoCredential, ServerAddress}
import com.typesafe.config.{ConfigFactory, Config}
import java.io.File

object ApplicationSettings extends Logging {
  def loadSettings: ApplicationSettings = {
    val fileLocations = List(
      System.getProperty("omatsivut.configFile"),
      "../module-install-parent/config/common/omatsivut/omatsivut.properties",
      "./module-install-parent/config/common/omatsivut/omatsivut.properties",
      System.getProperty("user.home") + "/oph-configuration/common.properties", // for server environments
      System.getProperty("user.home") + "/oph-configuration/omatsivut.properties"
    )
    fileLocations.flatMap(getFile).headOption match {
      case Some(configFile) =>
        logger.info("Using configuration file " + configFile)
        val config: Config = ConfigFactory.parseFile(configFile)
        val settings = ConfigFactory.load(config)
        val configReader = new ApplicationSettings(new SettingsReader() {
          def getProperty(key: String) = {
            settings.getString(key)
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
case class ApplicationSettings(config: SettingsReader) {
  val casTicketUrl = config getString "omatsivut.cas.ticket.url"

  val authenticationService = getRemoteApplicationConfig(config.getConfig("omatsivut.authentication-service"))

  val aesKey = config getString "omatsivut.crypto.aes.key"
  val hmacKey = config getString "omatsivut.crypto.hmac.key"

  private val hakuAppMongoHost = config getProperty "omatsivut.haku-app.mongo.host"
  private val hakuAppMongoPort = config getInt "omatsivut.haku-app.mongo.port"
  private val hakuAppMongoDbName = config getString "omatsivut.haku-app.mongo.db.name"
  private val hakuAppMongoDbUsername = config getString "omatsivut.haku-app.mongo.db.username"
  private val hakuAppMongoDbPassword = config getString "omatsivut.haku-app.mongo.db.password" toCharArray ()

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
  def getProperty(key: String): String
  def getInt(key: String): Int = Integer.parseInt(getProperty(key))
  def getString(key: String) = getProperty(key)
  def getConfig(key: String): SettingsReader = {
    val parent = this
    new SettingsReader {
      def getProperty(subKey: String) = {
        parent.getProperty(key + "." + subKey)
      }
    }
  }
}