package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.haku.testfixtures.MongoFixtureImporter
import fi.vm.sade.omatsivut.AppConfig.AppConfig

case class FixtureImporter(implicit val appConfig: AppConfig) {
  def applyFixtures(fixtureName: String = "") {
    MongoFixtureImporter.importJsonFixtures(appConfig.mongoTemplate, appConfig.springContext.applicationDAO)
    fixtureName match {
      case "peruskoulu" => PeruskouluFixture(appConfig).apply
      case _ =>
    }
  }
}

