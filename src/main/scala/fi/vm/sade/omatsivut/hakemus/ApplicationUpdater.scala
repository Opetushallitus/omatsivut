package fi.vm.sade.omatsivut.hakemus

import java.util.Date

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.domain.{QuestionId, Hakemus}
import fi.vm.sade.omatsivut.domain.Hakemus._

import scala.collection.JavaConversions._

object ApplicationUpdater {
  val preferencePhaseKey = OppijaConstants.PHASE_APPLICATION_OPTIONS

  def update(applicationSystem: ApplicationSystem)(application: Application, hakemus: Hakemus) {
    val updatedAnswers = getUpdatedAnswersForApplication(applicationSystem)(application, hakemus)
    updatedAnswers.foreach { case (phaseId, phaseAnswers) =>
      application.addVaiheenVastaukset(phaseId, phaseAnswers)
    }
    application.setUpdated(new Date(hakemus.updated))
  }

  def getUpdatedAnswersForApplication(applicationSystem: ApplicationSystem)(application: Application, hakemus: Hakemus): Answers = {
    val allAnswers = getAllUpdatedAnswersForApplication(applicationSystem)(application, hakemus)
    val removedAnswerIds = getRemovedAnswerIds(applicationSystem, application, hakemus)
    pruneOrphanedAnswers(removedAnswerIds, allAnswers)
  }

  def getAllUpdatedAnswersForApplication(applicationSystem: ApplicationSystem)(application: Application, hakemus: Hakemus): Answers = {
    allAnswersFromApplication(application) ++ updatedAnswersForHakuToiveet(applicationSystem, application, hakemus) ++ updatedAnswersForOtherPhases(application, hakemus)
  }

  private def pruneOrphanedAnswers(removedAnswerIds: Seq[QuestionId], answers: Answers): Answers = {
    answers.map { case (phaseId, phaseAnswers) =>
        (phaseId, phaseAnswers.filterKeys { case questionId =>
            !removedAnswerIds.contains(QuestionId(phaseId, questionId))
        })
    }
  }

  private def getRemovedAnswerIds(applicationSystem: ApplicationSystem, application: Application, hakemus: Hakemus): Seq[QuestionId] = {
    val allOldAnswers = allAnswersFromApplication(application)
    val allNewAnswers = getAllAnswersForApplication(applicationSystem, application, hakemus)

    val removedQuestions = AddedQuestionFinder.findAddedQuestions(applicationSystem, allOldAnswers, allNewAnswers).flatMap(_.flatten)
    removedQuestions.flatMap(_.answerIds)
  }

  def getAllAnswersForApplication(applicationSystem: ApplicationSystem, application: Application, hakemus: Hakemus): Answers = {
    allAnswersFromApplication(application) ++ updatedAnswersForHakuToiveet(applicationSystem, application, hakemus) ++ updatedAnswersForOtherPhases(application, hakemus)
  }

  private def allAnswersFromApplication(application: Application) = {
    application.getAnswers.toMap.mapValues(_.toMap)
  }

  private def updatedAnswersForOtherPhases(application: Application, hakemus: Hakemus): Answers = {
    val allOtherPhaseAnswers = hakemus.answers.filterKeys(phase => phase != preferencePhaseKey)
    allOtherPhaseAnswers.map { case (phase, answers) =>
      val existingAnswers = application.getPhaseAnswers(phase).toMap
      (phase, existingAnswers ++ answers)
    }.toMap
  }

  private def updatedAnswersForHakuToiveet(applicationSystem: ApplicationSystem, application: Application, hakemus: Hakemus): Answers = {
    val hakutoiveetAnswers: Map[String, String] = hakemus.answers.getOrElse(preferencePhaseKey, Map())
    val previousHakutoiveetAnswers: Map[String, String] = application.getPhaseAnswers(preferencePhaseKey).toMap

    val updatedAnswersForHakutoiveetPhase = HakutoiveetConverter.updateAnswers(hakemus.hakutoiveet, hakutoiveetAnswers, previousHakutoiveetAnswers)
    Map(preferencePhaseKey -> updatedAnswersForHakutoiveetPhase)
  }
}
