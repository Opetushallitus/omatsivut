package fi.vm.sade.omatsivut.fixtures

import com.mongodb.util.JSON
import scala.io.Source
import com.mongodb.casbah.Imports._
import fi.vm.sade.omatsivut.AppConfig

object FixtureUtils {

  def applyFixtures(): Unit = {

    val fixtureDir = new java.io.File("testfixtures")

    for (collection <- fixtureDir.listFiles if collection.isDirectory) {

      val coll = AppConfig.loadSettings.hakuAppMongoDb(collection.getName)

      for (file <- collection.listFiles if file.getName endsWith ".json") {
        val source = Source.fromFile(file)
        val lines = source.mkString
        source.close()
        val doc: MongoDBObject = JSON.parse(lines).asInstanceOf[DBObject]
        val query = doc filter {
          case (k, v) => k == "_id"
        }
        coll.remove(query)
        coll.insert(doc)
      }
    }
  }
}
