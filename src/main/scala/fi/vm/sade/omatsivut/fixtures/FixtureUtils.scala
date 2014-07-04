package fi.vm.sade.omatsivut.fixtures

import com.mongodb.util.JSON
import scala.io.Source
import com.mongodb.casbah.Imports._
import fi.vm.sade.omatsivut.{OmatSivutSpringContext, AppConfig}
import fi.vm.sade.haku.testfixtures.{MongoFixtureImporter, ProjectRootFinder}
import org.springframework.data.mongodb.core.MongoTemplate

object FixtureUtils {
  def applyFixtures(): Unit = {
    importHakemusApiFixtures
    importLocalFixtures
  }

  private def importHakemusApiFixtures {
    val mongoTemplate = OmatSivutSpringContext.context.getBean(classOf[MongoTemplate])
    MongoFixtureImporter.importJsonFixtures(mongoTemplate)
  }

  private def importLocalFixtures {
    val fixtureDir = new java.io.File("testfixtures")

    for (collection <- fixtureDir.listFiles if collection.isDirectory) {

      val coll = AppConfig.settings.hakuAppMongoDb(collection.getName)

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
