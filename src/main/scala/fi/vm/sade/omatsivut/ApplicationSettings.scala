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
        val settings: Config = ConfigFactory.load(ConfigFactory.parseFile(configFile))
        val applicationSettings = new ApplicationSettings(settings)
        applicationSettings
      case None =>
        throw new RuntimeException("Configuration file missing. Please set the omatsivut.configFile property correctly, or make sure you have one of the following: " + fileLocations)
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
class ApplicationSettings(config: Config) {
  val casTicketUrl = config getString "omatsivut.cas.ticket.url"

  val authenticationService = getRemoteApplicationConfig(config.getConfig("omatsivut.authentication-service"))

  val aesKey = config getString "omatsivut.crypto.aes.key"
  val hmacKey = config getString "omatsivut.crypto.hmac.key"

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

  def toProperties = {
    val keys = config.entrySet().toList.map(_.getKey)
    keys.map { key =>
      (key, config.getString(key))
    }.toMap
  }
}