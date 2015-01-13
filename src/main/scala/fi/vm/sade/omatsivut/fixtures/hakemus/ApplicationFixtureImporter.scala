package fi.vm.sade.omatsivut.fixtures.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.hakemus.domain.dto.SyntheticApplication
import fi.vm.sade.haku.oppija.hakemus.domain.dto.SyntheticApplication.Hakemus
import fi.vm.sade.haku.testfixtures.MongoFixtureImporter
import fi.vm.sade.omatsivut.config.OmatSivutSpringContext
import fi.vm.sade.omatsivut.fixtures.TestFixture

class ApplicationFixtureImporter(context: OmatSivutSpringContext) {

  def applyFixtures(fixtureName: String = "", selector: String = "**/*.json") {
    if (fixtureName == "erillishaku") {
      import collection.JavaConversions._
      MongoFixtureImporter.clearFixtures(context.mongoTemplate, context.applicationDAO, "application")
      val hakemus = new Hakemus(TestFixture.personOid, "Erillis", "Hakija", "010101-123N", "9.1.1995")
      val syntheticApplication = new SyntheticApplication("erilliskohde", "erillishaku", "erillistarjoaja", List(hakemus))
      context.syntheticApplicationService.createApplications(syntheticApplication)
    } else {
      if (!selector.endsWith("*.json")) {
        MongoFixtureImporter.clearFixtures(context.mongoTemplate, context.applicationDAO, "application")
      }
      MongoFixtureImporter.importJsonFixtures(context.mongoTemplate, context.applicationDAO, selector)
      applyOverrides(fixtureName)
    }
  }

  def applyOverrides(fixtureName: String = "") {
    fixtureName match {
      case "peruskoulu" => new PeruskouluFixture(context.applicationDAO).apply
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

