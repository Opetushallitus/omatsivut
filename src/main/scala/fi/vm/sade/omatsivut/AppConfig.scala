package fi.vm.sade.omatsivut

import com.typesafe.config._
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.casbah.MongoClient

object AppConfig {
 
  def loadSettings(): Settings = { 
 
    /** ConfigFactory.load() defaults to the following in order: 
      * system properties 
      * omatsivut.properties 
      * reference.conf 
      */ 
    new Settings(ConfigFactory.load("omatsivut"))
  }
  
  class Settings(config: Config) { 
    val casTicketUrl = config getString "omatsivut.cas.ticket.url" 
    val hakuAppUsername = config getString "omatsivut.haku-app.username" 
    val hakuAppPassword = config getString "omatsivut.haku-app.password" 
    val hakuAppUrl = config getString "omatsivut.haku-app.url"
    val hakuAppHakuQuery = config getString "omatsivut.haku-app.haku.query"
    val hakuAppTicketConsumer = config getString "omatsivut.haku-app.ticket.consumer.query"
    
    private val hakuAppMongoHost  = config getString "omatsivut.haku-app.mongo.host"
    private val hakuAppMongoPort  = config getInt "omatsivut.haku-app.mongo.port"
    val hakuAppMongoDb  = config getString "omatsivut.haku-app.mongo.db.name"
    private val hakuAppMongoDbUsername  = config getString "omatsivut.haku-app.mongo.db.username"
    private val hakuAppMongoDbPassword  = config getString "omatsivut.haku-app.mongo.db.password" toCharArray()
    
    def hakuAppMongoClient: MongoClient = {
      val mongoAddress = new ServerAddress(hakuAppMongoHost, hakuAppMongoPort)
      if(hakuAppMongoDbUsername.isEmpty()) {
        MongoClient(mongoAddress)
      }
      else {
        MongoClient(
          List(mongoAddress),
          List(MongoCredential.createMongoCRCredential(hakuAppMongoDbUsername, hakuAppMongoDb, hakuAppMongoDbPassword))
        )
      }
    }
  }   
}