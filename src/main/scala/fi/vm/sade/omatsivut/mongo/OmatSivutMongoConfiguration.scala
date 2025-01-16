package fi.vm.sade.omatsivut.mongo

import com.mongodb.ConnectionString
import com.mongodb.client.{MongoClient, MongoClients}
import fi.vm.sade.omatsivut.util.Logging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.data.mongodb.core.MongoTemplate

@Configuration class OmatSivutMongoConfiguration extends Logging {

  @Bean def mongoTemplate(mongoClient: MongoClient, @Value("${mongo.db.name}") databaseName: String): MongoTemplate = {
    new MongoTemplate(mongoClient, databaseName)
  }

  def sanitizeMongoUrl(mongoUri: String) = mongoUri match {
    case uri if uri.contains("@") => uri.substring(mongoUri.indexOf("@"))
    case uri if uri.contains("//") => uri.substring(mongoUri.indexOf("//"))
    case _ =>
  }

  @Bean def mongoClient(@Value("${mongodb.url}") mongoUri: String): MongoClient = {
    logger.info("Creating MongoClient for server(s): " + sanitizeMongoUrl(mongoUri))
    val connectionString: ConnectionString = new ConnectionString(mongoUri)
    MongoClients.create(connectionString)
  }



}
