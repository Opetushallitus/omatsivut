package fi.vm.sade.omatsivut.hakemus

import java.util.Date

import fi.vm.sade.omatsivut.domain.Language

import scala.collection.JavaConversions._

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants

import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.hakemus.domain.{HakemusMuutos, QuestionId, AnswerId, Hakemus}
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus._

object ApplicationUpdater {
  val preferencePhaseKey = OppijaConstants.PHASE_APPLICATION_OPTIONS

  /**
   * NOTE this method mutates the application, so call with application.clone(), if it is not wanted.
   */
  def update(applicationSystem: ApplicationSystem)(application: Application, hakemus: HakemusMuutos)(implicit lang: Language.Language) = {
    val updatedAnswers = getUpdatedAnswersForApplication(applicationSystem)(application, hakemus)
    updatedAnswers.foreach { case (phaseId, phaseAnswers) =>
      application.addVaiheenVastaukset(phaseId, phaseAnswers)
    }
    application.setUpdated(new Date())
    application
  }

  private def getUpdatedAnswersForApplication(applicationSystem: ApplicationSystem)(application: Application, hakemus: HakemusMuutos)(implicit lang: Language.Language): Answers = {
    val allAnswers = getAllUpdatedAnswersForApplication(applicationSystem)(application, hakemus)
    val removedAnswerIds = getRemovedAnswerIds(applicationSystem, application, hakemus)

    applyHiddenValues(applicationSystem)(pruneOrphanedAnswers(removedAnswerIds, allAnswers))
  }

  private def applyHiddenValues(applicationSystem: ApplicationSystem)(allAnswers: Answers): Answers = {
    val vals: Set[(QuestionId, String)] = FormQuestionFinder.findHiddenValues(ElementWrapper.wrapFiltered(applicationSystem.getForm, HakemusConverter.flattenAnswers(allAnswers)))
    vals.foldLeft(allAnswers) { case (answers: Answers, (question: QuestionId, answer: String)) =>
        updateSingleAnswer(answers, question, answer)
    }
  }

  def getAllUpdatedAnswersForApplication(applicationSystem: ApplicationSystem)(application: Application, hakemus: HakemusMuutos): Answers = {
    allAnswersFromApplication(application).filterKeys(_ != preferencePhaseKey) ++ updatedAnswersForHakuToiveet(applicationSystem, application, hakemus) ++ updatedAnswersForOtherPhases(application, hakemus)
  }

  private def updateSingleAnswer(answers: Answers, question: QuestionId, answer: String) = {
    answers.map { case (phase, phaseAnswers) =>
      if (phase == question.phaseId)
        (phase, phaseAnswers)
      else
        (phase, phaseAnswers + (question.questionId -> answer))
    }
  }

  private def pruneOrphanedAnswers(removedAnswerIds: Seq[AnswerId], answers: Answers): Answers = {
    answers.map { case (phaseId, phaseAnswers) =>
        (phaseId, phaseAnswers.filterKeys { case answerId =>
            !removedAnswerIds.contains(AnswerId(phaseId, answerId))
        })
    }
  }

  private def getRemovedAnswerIds(applicationSystem: ApplicationSystem, application: Application, hakemus: HakemusMuutos)(implicit lang: Language.Language): Seq[AnswerId] = {
    val allOldAnswers = allAnswersFromApplication(application)
    val allNewAnswers = getAllAnswersForApplication(applicationSystem, application, hakemus)

    val removedQuestions = AddedQuestionFinder.findAddedQuestions(applicationSystem, allOldAnswers, allNewAnswers).toList
    removedQuestions.flatMap(_.answerIds)
  }

  def getAllAnswersForApplication(applicationSystem: ApplicationSystem, application: Application, hakemus: HakemusMuutos): Answers = {
    allAnswersFromApplication(application) ++ updatedAnswersForHakuToiveet(applicationSystem, application, hakemus) ++ updatedAnswersForOtherPhases(application, hakemus)
  }

  def allAnswersFromApplication(application: Application) = {
    application.getAnswers.toMap.mapValues(_.toMap)
  }

  private def updatedAnswersForOtherPhases(application: Application, hakemus: HakemusMuutos): Answers = {
    val allOtherPhaseAnswers = hakemus.answers.filterKeys(phase => phase != preferencePhaseKey)
    allOtherPhaseAnswers.map { case (phase, answers) =>
      val existingAnswers = application.getPhaseAnswers(phase).toMap
      (phase, existingAnswers ++ answers)
    }.toMap
  }

  private def updatedAnswersForHakuToiveet(applicationSystem: ApplicationSystem, application: Application, hakemus: HakemusMuutos): Answers = {
    val newAnswers = hakemus.answers
    val previousAnswers = allAnswersFromApplication(application)

    val updatedAnswersForHakutoiveetPhase = HakutoiveetConverter.updateAnswers(hakemus.hakutoiveet, newAnswers, previousAnswers)
    Map(preferencePhaseKey -> updatedAnswersForHakutoiveetPhase)
  }
}
