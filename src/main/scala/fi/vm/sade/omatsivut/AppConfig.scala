package fi.vm.sade.omatsivut

import com.typesafe.config._
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.casbah.MongoClient
import java.io.File

object AppConfig extends Logging {
  val loadSettings: Settings = {
    val fileLocations = List(
      System.getProperty("omatsivut.configFile"),
      System.getProperty("user.home") + "/oph-configuration/common.properties", // for server environments
      System.getProperty("user.home") + "/oph-configuration/omatsivut.properties",
      "../module-install-parent/config/common/omatsivut/omatsivut.properties",
      "./module-install-parent/config/common/omatsivut/omatsivut.properties")
    fileLocations.flatMap(getFile).headOption match {
      case Some(configFile) =>
        logger.info("Using configuration file " + configFile)
        val config = ConfigFactory.parseFile(configFile)
        val settings = new Settings(ConfigFactory.load(config))
        logger.info("Settings: " + settings)
        settings
      case None =>
        throw new RuntimeException("Configuration file missing. Please set the omatsivut.configFile property correctly, or make sure you have ../module-install-parent or ~/oph-configuration/omatsivut.properties")
    }
  }

  def getFile(name: String): List[File] = {
    if (name != null && new File(name).exists) {
      List(new File(name))
    } else {
      Nil
    }
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