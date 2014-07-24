package fi.vm.sade.omatsivut.mongo

import com.mongodb.{Mongo, MongoClient, MongoClientOptions, MongoClientURI, WriteConcern}
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.data.mongodb.core.MongoTemplate

@Configuration class OmatSivutMongoConfiguration {
  @Bean def mongoTemplate(mongo: Mongo, @Value("${mongo.db.name}") databaseName: String): MongoTemplate = {
    new MongoTemplate(mongo, databaseName)
  }

  @Bean def mongo(@Value("${mongodb.url}") mongoUri: String): MongoClient = {
    val options = new MongoClientOptions.Builder().writeConcern(WriteConcern.FSYNCED)
    val mongoClientURI: MongoClientURI = new MongoClientURI(mongoUri, options)
    val mongoClient: MongoClient = new MongoClient(mongoClientURI)
    mongoClient
  }
}