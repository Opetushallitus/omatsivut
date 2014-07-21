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
    val removedQuestionIds = getRemovedQuestionIds(applicationSystem, application, hakemus)
    pruneOrphanedAnswers(removedQuestionIds, allAnswers)
  }

  def getAllUpdatedAnswersForApplication(applicationSystem: ApplicationSystem)(application: Application, hakemus: Hakemus): Answers = {
    allAnswersFromApplication(application) ++ updatedAnswersForHakuToiveet(application, hakemus) ++ updatedAnswersForOtherPhases(application, hakemus)
  }


  private def pruneOrphanedAnswers(removedQuestions: Seq[QuestionId], answers: Answers): Answers = {
    answers.map { case (phaseId, phaseAnswers) =>
        (phaseId, phaseAnswers.filterKeys { case questionId =>
            !removedQuestions.contains(QuestionId(phaseId, questionId))
        })
    }
  }

  private def getRemovedQuestionIds(applicationSystem: ApplicationSystem, application: Application, hakemus: Hakemus): Seq[QuestionId] = {
    val allOldAnswers = allAnswersFromApplication(application)
    val allNewAnswers = getAllAnswersForApplication(application, hakemus)

    val removedQuestions = AddedQuestionFinder.findAddedQuestions(applicationSystem, allOldAnswers, allNewAnswers).flatMap(_.flatten)
    val addedQuestions = AddedQuestionFinder.findAddedQuestions(applicationSystem, allOldAnswers, allNewAnswers).flatMap(_.flatten)
    removedQuestions.map(_.id)
  }

  private def getAllAnswersForApplication(application: Application, hakemus: Hakemus): Answers = {
    allAnswersFromApplication(application) ++ updatedAnswersForHakuToiveet(application, hakemus) ++ updatedAnswersForOtherPhases(application, hakemus)
  }

  private def allAnswersFromApplication(application: Application) = {
    application.getAnswers.toMap.mapValues(_.toMap)
  }


  private def removeOrphanedAnswers(applicationSystem: ApplicationSystem, application: Application, newAnswers: Answers): Map[String, Map[String, String]] = {
    val oldAnswers: Answers = application.getAnswers.toMap.mapValues(_.toMap)
    val removedQuestions = AddedQuestionFinder.findAddedQuestions(applicationSystem, oldAnswers, newAnswers).flatMap(_.flatten)
    val removedQuestionIds: Seq[QuestionId] = removedQuestions.map(_.id)
    newAnswers.map { case (phaseId, phaseAnswers) =>
      (phaseId, phaseAnswers.filterKeys { questionId =>
        !removedQuestionIds.contains(QuestionId(phaseId, questionId))
      })
    }
  }

  private def updatedAnswersForOtherPhases(application: Application, hakemus: Hakemus): Answers = {
    val allOtherPhaseAnswers = hakemus.answers.filterKeys(phase => phase != preferencePhaseKey)
    allOtherPhaseAnswers.map { case (phase, answers) =>
      val existingAnswers = application.getPhaseAnswers(phase).toMap
      (phase, existingAnswers ++ answers)
    }.toMap
  }

  private def updatedAnswersForHakuToiveet(application: Application, hakemus: Hakemus): Answers = {
    val updatedAnswersForHakutoiveetPhase = HakutoiveetConverter.updateAnswers(hakemus, application.getPhaseAnswers(preferencePhaseKey).toMap)
    Map(preferencePhaseKey -> updatedAnswersForHakutoiveetPhase)
  }
}
