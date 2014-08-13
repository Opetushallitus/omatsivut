package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.haku.testfixtures.MongoFixtureImporter
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.haku.oppija.hakemus.domain.Application

case class FixtureImporter(implicit val appConfig: AppConfig) {
  def applyFixtures(fixtureName: String = "") {
    MongoFixtureImporter.importJsonFixtures(appConfig.mongoTemplate, appConfig.springContext.applicationDAO)
    applyOverrides(fixtureName)
  }

  def applyOverrides(fixtureName: String = "") {
    fixtureName match {
      case "peruskoulu" => PeruskouluFixture(appConfig).apply
      case "passiveApplication" => ApplicationStateFixture(appConfig).apply(Application.State.PASSIVE)
      case "incompleteApplication" => ApplicationStateFixture(appConfig).apply(Application.State.INCOMPLETE)
      case "submittedApplication" => ApplicationStateFixture(appConfig).apply(Application.State.SUBMITTED)
      case _ =>
    }
  }
}

