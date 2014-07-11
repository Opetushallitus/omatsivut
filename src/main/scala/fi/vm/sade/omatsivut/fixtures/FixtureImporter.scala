package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.haku.testfixtures.MongoFixtureImporter
import org.springframework.data.mongodb.core.MongoTemplate

object FixtureImporter {
  def importFixtures(mongoTemplate: MongoTemplate): Unit = {
    MongoFixtureImporter.importJsonFixtures(mongoTemplate)
  }
}
