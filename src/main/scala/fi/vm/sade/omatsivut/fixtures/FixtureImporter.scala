package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.haku.testfixtures.MongoFixtureImporter
import fi.vm.sade.omatsivut.config.AppConfig
import AppConfig.AppConfig
import fi.vm.sade.haku.oppija.hakemus.domain.Application

case class FixtureImporter(implicit val appConfig: AppConfig) {
  def applyFixtures(fixtureName: String = "") {
    MongoFixtureImporter.importJsonFixtures(appConfig.mongoTemplate, appConfig.springContext.applicationDAO)
    applyOverrides(fixtureName)
  }

  def applyOverrides(fixtureName: String = "") {
    fixtureName match {
      case "peruskoulu" => PeruskouluFixture(appConfig).apply
      case "passiveApplication" => ApplicationStateFixture(appConfig).setState(Application.State.PASSIVE)
      case "incompleteApplication" => ApplicationStateFixture(appConfig).setState(Application.State.INCOMPLETE)
      case "submittedApplication" => ApplicationStateFixture(appConfig).setState(Application.State.SUBMITTED)
      case "kymppiluokka" => KymppiluokkaFixture(appConfig).apply
      case "postProcessingFailed" => ApplicationStateFixture(appConfig).setPostProcessingState(Application.PostProcessingState.FAILED)
      case "postProcessingDone" => ApplicationStateFixture(appConfig).setPostProcessingState(Application.PostProcessingState.DONE)
      case _ =>
    }
  }
}

