package fi.vm.sade.omatsivut

import com.typesafe.config._
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.casbah.MongoClient
import java.io.File

object AppConfig extends Logging {
  val loadSettings: Settings = {
    val configFileName = System.getProperty("omatsivut.configFile", System.getProperty("user.home") + "/oph-configuration/omatsivut.properties")
    val configFile: File = new File(configFileName)
    if (configFile.exists) {
      logger.info("Using configuration file " + configFile)
    } else {
      throw new RuntimeException("Configuration file " + configFile + " missing. Please set the omatsivut.configFile property correctly")
    }
    val config = ConfigFactory.parseFile(configFile)
    /**
     * ConfigFactory.load() defaults to the following in order:
     * system properties
     * omatsivut.properties
     * reference.conf
     */
    val settings = new Settings(ConfigFactory.load(config))
    logger.info("Settings: " + settings)
    settings
  }
}

case class RemoteApplicationConfig(url: String, username: String, password: String, path: String, ticketConsumerPath: String)

case class Settings(config: Config) {
  val casTicketUrl = config getString "omatsivut.cas.ticket.url"

  val hakuApp = getRemoteApplicationConfig(config.getConfig("omatsivut.haku-app"))

  val authenticationService = getRemoteApplicationConfig(config.getConfig("omatsivut.authentication-service"))

  private val hakuAppMongoHost = config getString "omatsivut.haku-app.mongo.host"
  private val hakuAppMongoPort = config getInt "omatsivut.haku-app.mongo.port"
  private val hakuAppMongoDbName = config getString "omatsivut.haku-app.mongo.db.name"
  private val hakuAppMongoDbUsername = config getString "omatsivut.haku-app.mongo.db.username"
  private val hakuAppMongoDbPassword = config getString "omatsivut.haku-app.mongo.db.password" toCharArray ()

  private def getRemoteApplicationConfig(config: Config) = {
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