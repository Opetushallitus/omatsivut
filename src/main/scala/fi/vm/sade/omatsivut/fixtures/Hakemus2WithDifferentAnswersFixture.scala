package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.omatsivut.config.AppConfig
import AppConfig.AppConfig
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus.Answers

class Hakemus2WithDifferentAnswersFixture(appConfig: AppConfig) {
  private val dao = appConfig.springContext.applicationDAO

  def apply(answers: Answers) {
    val application: Application = dao.find(new Application().setOid(TestFixture.hakemus2)).get(0)
    updateAnswers(application, answers)
    dao.save(application)
  }

  private def updateAnswers(application: Application, answers: Answers) {
    import scala.collection.JavaConverters._
    answers.foreach { case (phaseId, phaseAnswers) =>
      application.addVaiheenVastaukset(phaseId, phaseAnswers.asJava)
    }
  }
}
