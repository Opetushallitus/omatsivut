package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.hakemus.it.dao.ApplicationDAO
import fi.vm.sade.omatsivut.config.AppConfig
import AppConfig.AppConfig
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus.Answers

class HakemusWithDifferentAnswersFixture(hakemusOid: String)(val dao: ApplicationDAO) {
  def replaceAnswers(answers: Answers) {
    val application: Application = dao.find(new Application().setOid(hakemusOid)).get(0)
    replaceAnswers(application, answers)
    dao.save(application)
  }

  def addAnswers(answers: Answers) {
    val application: Application = dao.find(new Application().setOid(hakemusOid)).get(0)
    addAnswers(application, answers)
    dao.save(application)
  }

  private def addAnswers(application: Application, answers: Answers) {
    import scala.collection.JavaConverters._
    answers.foreach { case (phaseId, phaseAnswers) =>
      val oldPhaseAnswers = application.getPhaseAnswers(phaseId).asScala
      val newAnswers = phaseAnswers.foldLeft(oldPhaseAnswers) { case (memo, value: (String, String)) => memo + (value._1 -> value._2)}
      application.addVaiheenVastaukset(phaseId, newAnswers.asJava)
    }
    val i=2
  }

  private def replaceAnswers(application: Application, answers: Answers) {
    import scala.collection.JavaConverters._
    answers.foreach { case (phaseId, phaseAnswers) =>
      application.addVaiheenVastaukset(phaseId, phaseAnswers.asJava)
    }
  }
}
