package fi.vm.sade.omatsivut.fixtures.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.hakemus.it.dao.ApplicationDAO
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.Answers

protected class HakemusWithDifferentAnswersFixture(hakemusOid: String)(val dao: ApplicationDAO) {
  def replaceAnswers(answers: Answers) {
    val application: Application = dao.find(new Application().setOid(hakemusOid)).get(0)
    replaceAnswers(application, answers)
    dao.update(new Application().setOid(application.getOid), application)
  }

  def addAnswers(answers: Answers) {
    val application: Application = dao.find(new Application().setOid(hakemusOid)).get(0)
    addAnswers(application, answers)
    dao.update(new Application().setOid(application.getOid), application)
  }

  private def addAnswers(application: Application, answers: Answers) {
    import scala.collection.JavaConverters._
    answers.foreach { case (phaseId, phaseAnswers) =>
      val oldPhaseAnswers = application.getPhaseAnswers(phaseId).asScala
      val newAnswers = phaseAnswers.foldLeft(oldPhaseAnswers) { case (memo, value: (String, String)) => memo + (value._1 -> value._2)}
      application.setVaiheenVastauksetAndSetPhaseId(phaseId, newAnswers.asJava)
    }
    val i=2
  }

  private def replaceAnswers(application: Application, answers: Answers) {
    import scala.collection.JavaConverters._
    answers.foreach { case (phaseId, phaseAnswers) =>
      application.setVaiheenVastauksetAndSetPhaseId(phaseId, phaseAnswers.asJava)
    }
  }
}
