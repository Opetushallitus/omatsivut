package fi.vm.sade.omatsivut.fixtures.hakemus

import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.Answers
import fi.vm.sade.hakemuseditori.hakemus.hakuapp.domain.Application

protected class HakemusWithDifferentAnswersFixture(hakemusOid: String) {

  def addAnswers(application: Application, answers: Answers) {
    import scala.collection.JavaConverters._
    answers.foreach { case (phaseId, phaseAnswers) =>
      val oldPhaseAnswers = application.getPhaseAnswers(phaseId).asScala
      val newAnswers = phaseAnswers.foldLeft(oldPhaseAnswers) { case (memo, value: (String, String)) => memo + (value._1 -> value._2)}
      application.setVaiheenVastauksetAndSetPhaseId(phaseId, newAnswers.asJava)
    }
    val i=2
  }

  def replaceAnswers(application: Application, answers: Answers) {
    import scala.collection.JavaConverters._
    answers.foreach { case (phaseId, phaseAnswers) =>
      application.setVaiheenVastauksetAndSetPhaseId(phaseId, phaseAnswers.asJava)
    }
  }
}
