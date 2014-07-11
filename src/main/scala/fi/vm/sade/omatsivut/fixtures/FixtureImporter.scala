package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.haku.testfixtures.MongoFixtureImporter
import fi.vm.sade.omatsivut.OmatSivutSpringContext

object FixtureImporter {
  def importFixtures(): Unit = {
    MongoFixtureImporter.importJsonFixtures(OmatSivutSpringContext.context.mongoTemplate)
  }
}
