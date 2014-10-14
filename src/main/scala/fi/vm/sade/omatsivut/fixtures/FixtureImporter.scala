package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.hakemus.it.dao.ApplicationDAO
import fi.vm.sade.haku.testfixtures.MongoFixtureImporter
import org.springframework.data.mongodb.core.MongoTemplate

class FixtureImporter(val applicationDAO: ApplicationDAO, val mongoTemplate: MongoTemplate) {
  def applyFixtures(fixtureName: String = "", selector: String = "**/*.json") {
    if (!selector.endsWith("*.json")) {
      MongoFixtureImporter.clearFixtures(mongoTemplate, applicationDAO, "application")
    }
    MongoFixtureImporter.importJsonFixtures(mongoTemplate, applicationDAO, selector)
    applyOverrides(fixtureName)
  }

  def applyOverrides(fixtureName: String = "") {
    fixtureName match {
      case "peruskoulu" => new PeruskouluFixture(applicationDAO).apply
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

