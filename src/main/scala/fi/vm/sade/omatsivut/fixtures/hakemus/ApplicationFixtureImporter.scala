package fi.vm.sade.omatsivut.fixtures.hakemus

import fi.vm.sade.hakemuseditori.hakemus.HakemusSpringContext
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.testfixtures.MongoFixtureImporter

class ApplicationFixtureImporter(context: HakemusSpringContext) {

  def applyFixtures(fixtureName: String = "", selector: String = "**/*.json") {
    if (!selector.endsWith("*.json")) {
      MongoFixtureImporter.clearFixtures(context.mongoTemplate, context.applicationDAO, "application")
    }
    MongoFixtureImporter.importJsonFixtures(context.mongoTemplate, context.applicationDAO, selector)
    applyOverrides(fixtureName)
  }

  def applyOverrides(fixtureName: String = "") {
    fixtureName match {
      case "peruskoulu" => new PeruskouluFixture(context.applicationDAO).apply
      case "peruskouluWithMissingPreferences" => new PeruskouluWithMissingPreferencesFixture(context.applicationDAO).apply
      case "passiveApplication" => new ApplicationStateFixture(context.applicationDAO).setState(Application.State.PASSIVE)
      case "incompleteApplication" => new ApplicationStateFixture(context.applicationDAO).setState(Application.State.INCOMPLETE)
      case "submittedApplication" => new ApplicationStateFixture(context.applicationDAO).setState(Application.State.SUBMITTED)
      case "kymppiluokka" => new KymppiluokkaFixture(context.applicationDAO).apply
      case "postProcessingFailed" => new ApplicationStateFixture(context.applicationDAO).setPostProcessingState(Application.PostProcessingState.FAILED)
      case "postProcessingDone" => new ApplicationStateFixture(context.applicationDAO).setPostProcessingState(Application.PostProcessingState.DONE)
      case _ =>
    }
  }
}

