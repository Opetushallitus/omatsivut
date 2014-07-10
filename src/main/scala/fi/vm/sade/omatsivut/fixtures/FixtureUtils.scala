package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.haku.testfixtures.MongoFixtureImporter
import fi.vm.sade.omatsivut.OmatSivutSpringContext
import org.springframework.data.mongodb.core.MongoTemplate

object FixtureUtils {
  def applyFixtures(): Unit = {
    importHakemusApiFixtures
  }

  private def importHakemusApiFixtures {
    val mongoTemplate = OmatSivutSpringContext.context.mongoTemplate
    MongoFixtureImporter.importJsonFixtures(mongoTemplate)
  }
}
