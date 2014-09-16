package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.haku.testfixtures.MongoFixtureImporter
import fi.vm.sade.omatsivut.config.{OmatSivutSpringContext, AppConfig}
import AppConfig.AppConfig
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import org.springframework.data.mongodb.core.MongoTemplate

class FixtureImporter(val appConfig: AppConfig) {
  val springContext: OmatSivutSpringContext = appConfig.springContext
  val applicationDAO = springContext.applicationDAO
  val monogTemplate: MongoTemplate = springContext.mongoTemplate


  def applyFixtures(fixtureName: String = "") {
    MongoFixtureImporter.importJsonFixtures(monogTemplate, applicationDAO)
    applyOverrides(fixtureName)
  }

  def applyOverrides(fixtureName: String = "") {
    fixtureName match {
      case "peruskoulu" => new PeruskouluFixture(applicationDAO).apply
      case "lisahakuEnded" => new LisahakuEndedFixture(applicationDAO).apply
      case "passiveApplication" => new ApplicationStateFixture(applicationDAO).setState(Application.State.PASSIVE)
      case "incompleteApplication" => new ApplicationStateFixture(applicationDAO).setState(Application.State.INCOMPLETE)
      case "submittedApplication" => new ApplicationStateFixture(applicationDAO).setState(Application.State.SUBMITTED)
      case "kymppiluokka" => new KymppiluokkaFixture(applicationDAO).apply
      case "postProcessingFailed" => new ApplicationStateFixture(applicationDAO).setPostProcessingState(Application.PostProcessingState.FAILED)
      case "postProcessingDone" => new ApplicationStateFixture(applicationDAO).setPostProcessingState(Application.PostProcessingState.DONE)
      case _ =>
    }
  }
}

