package fi.vm.sade.omatsivut.mongo

import com.mongodb.{Mongo, MongoClient, MongoClientOptions, MongoClientURI, WriteConcern}
import fi.vm.sade.utils.slf4j.Logging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.data.mongodb.core.MongoTemplate

@Configuration class OmatSivutMongoConfiguration extends Logging {

  @Bean def mongoTemplate(mongo: Mongo, @Value("${mongo.db.name}") databaseName: String): MongoTemplate = {
    new MongoTemplate(mongo, databaseName)
  }

  def sanitizeMongoUrl(mongoUri: String) = mongoUri match {
    case uri if uri.contains("@") => uri.substring(mongoUri.indexOf("@"))
    case uri if uri.contains("//") => uri.substring(mongoUri.indexOf("//"))
    case _ =>
  }

  @Bean def mongo(@Value("${mongodb.url}") mongoUri: String): MongoClient = {
    logger.info("Creating MongoClient for server(s): " + sanitizeMongoUrl(mongoUri))
    val options = new MongoClientOptions.Builder().writeConcern(WriteConcern.FSYNCED)
    val mongoClientURI: MongoClientURI = new MongoClientURI(mongoUri, options)
    val mongoClient: MongoClient = new MongoClient(mongoClientURI)
    mongoClient
  }
}
