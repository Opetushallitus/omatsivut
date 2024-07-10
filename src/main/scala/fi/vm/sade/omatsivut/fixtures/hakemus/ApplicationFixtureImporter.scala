package fi.vm.sade.omatsivut.fixtures.hakemus

import fi.vm.sade.hakemuseditori.hakemus.HakemusSpringContext
import fi.vm.sade.omatsivut.fixtures.TestFixture.{hakemusNivelKesa2013WithPeruskouluBaseEducation, hakemusYhteishakuKevat2014WithForeignBaseEducation}

class ApplicationFixtureImporter(context: HakemusSpringContext) {

  def applyFixtures(fixtureName: String = "", selector: String = "**/*.json") {
    List(hakemusYhteishakuKevat2014WithForeignBaseEducation,hakemusNivelKesa2013WithPeruskouluBaseEducation)
    // TODO mockaa
//    if (!selector.endsWith("*.json")) {
//      MongoFixtureImporter.clearFixtures(context.mongoTemplate, context.applicationDAO, "application")
//    }
//    MongoFixtureImporter.importJsonFixtures(context.mongoTemplate, context.applicationDAO, selector)
    applyOverrides(fixtureName)
  }

  def applyOverrides(fixtureName: String = "") {
    fixtureName match {
      case "peruskoulu" => new PeruskouluFixture().apply
      //case "passiveApplication" => new ApplicationStateFixture(context.applicationDAO).setState(Application.State.PASSIVE)
      //case "incompleteApplication" => new ApplicationStateFixture(context.applicationDAO).setState(Application.State.INCOMPLETE)
      //case "submittedApplication" => new ApplicationStateFixture(context.applicationDAO).setState(Application.State.SUBMITTED)
      case "kymppiluokka" => new KymppiluokkaFixture().apply
      //case "postProcessingFailed" => new ApplicationStateFixture(context.applicationDAO).setPostProcessingState(Application.PostProcessingState.FAILED)
      //case "postProcessingDone" => new ApplicationStateFixture(context.applicationDAO).setPostProcessingState(Application.PostProcessingState.DONE)
      case _ =>
    }
  }
}

