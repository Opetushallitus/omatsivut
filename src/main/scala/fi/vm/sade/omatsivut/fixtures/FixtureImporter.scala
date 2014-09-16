package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.haku.testfixtures.MongoFixtureImporter
import fi.vm.sade.omatsivut.config.AppConfig
import AppConfig.AppConfig
import fi.vm.sade.haku.oppija.hakemus.domain.Application

class FixtureImporter(val appConfig: AppConfig) {
  def applyFixtures(fixtureName: String = "") {
    MongoFixtureImporter.importJsonFixtures(appConfig.mongoTemplate, appConfig.springContext.applicationDAO)
    applyOverrides(fixtureName)
  }

  def applyOverrides(fixtureName: String = "") {
    fixtureName match {
      case "peruskoulu" => new PeruskouluFixture(appConfig).apply
      case "lisahakuEnded" => new LisahakuEndedFixture(appConfig).apply
      case "passiveApplication" => new ApplicationStateFixture(appConfig).setState(Application.State.PASSIVE)
      case "incompleteApplication" => new ApplicationStateFixture(appConfig).setState(Application.State.INCOMPLETE)
      case "submittedApplication" => new ApplicationStateFixture(appConfig).setState(Application.State.SUBMITTED)
      case "kymppiluokka" => new KymppiluokkaFixture(appConfig).apply
      case "postProcessingFailed" => new ApplicationStateFixture(appConfig).setPostProcessingState(Application.PostProcessingState.FAILED)
      case "postProcessingDone" => new ApplicationStateFixture(appConfig).setPostProcessingState(Application.PostProcessingState.DONE)
      case _ =>
    }
  }
}

