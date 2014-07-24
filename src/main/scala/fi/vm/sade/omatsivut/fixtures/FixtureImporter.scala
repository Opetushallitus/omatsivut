package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.haku.testfixtures.MongoFixtureImporter
import fi.vm.sade.omatsivut.AppConfig.AppConfig

case class FixtureImporter(implicit val appConfig: AppConfig) {
  private val dao = appConfig.springContext.applicationDAO

  def applyFixtures {
    MongoFixtureImporter.importJsonFixtures(appConfig.mongoTemplate, appConfig.springContext.applicationDAO)
  }
}
