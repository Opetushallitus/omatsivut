package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.hakemus.ImmutableLegacyApplicationWrapper
import ImmutableLegacyApplicationWrapper.LegacyApplicationAnswers
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus._
import fi.vm.sade.omatsivut.hakemus.domain.{Hakemus, HakemusLike}
import fi.vm.sade.omatsivut.lomake.domain.{AnswerId, Lomake, QuestionId}
import fi.vm.sade.omatsivut.lomake.{AddedQuestionFinder, ElementWrapper, FormQuestionFinder}

object ApplicationUpdater {
  val preferencePhaseKey = OppijaConstants.PHASE_APPLICATION_OPTIONS

  def getUpdatedAnswersForApplication(lomake: Lomake, application: ImmutableLegacyApplicationWrapper, hakemus: HakemusLike)(implicit lang: Language.Language): LegacyApplicationAnswers = {
    val allAnswers = getAllUpdatedAnswersForApplication(lomake, application, hakemus.answers, hakemus.preferences)
    val removedAnswerIds = getRemovedAnswerIds(lomake, application, hakemus)

    applyHiddenValues(lomake, pruneOrphanedAnswers(removedAnswerIds, allAnswers))
  }

  private def applyHiddenValues(lomake: Lomake, allAnswers: Answers): Answers = {
    val vals: Set[(QuestionId, String)] = FormQuestionFinder.findHiddenValues(ElementWrapper.wrapFiltered(lomake.form, FlatAnswers.flatten(allAnswers)))
    vals.foldLeft(allAnswers) { case (answers: Answers, (question: QuestionId, answer: String)) =>
        updateSingleAnswer(answers, question, answer)
    }
  }

  def getAllUpdatedAnswersForApplication(lomake: Lomake, application: ImmutableLegacyApplicationWrapper, newAnswers: Hakemus.Answers, hakutoiveet: List[Hakemus.HakutoiveData]): Answers = {
    allAnswersFromApplication(application).filterKeys(_ != preferencePhaseKey) ++
      updatedAnswersForHakuToiveet(lomake, application, newAnswers, hakutoiveet) ++
      updatedAnswersForOtherPhases(application, newAnswers)
  }

  private def updateSingleAnswer(answers: Answers, question: QuestionId, answer: String) = {
    answers.map { case (phase, phaseAnswers) =>
      if (phase == question.phaseId)
        (phase, phaseAnswers + (question.questionId -> answer))
      else
        (phase, phaseAnswers)
    }
  }

  private def pruneOrphanedAnswers(removedAnswerIds: Seq[AnswerId], answers: Answers): Answers = {
    answers.map { case (phaseId, phaseAnswers) =>
        (phaseId, phaseAnswers.filterKeys { case answerId =>
            !removedAnswerIds.contains(AnswerId(phaseId, answerId))
        })
    }
  }

  private def getRemovedAnswerIds(lomake: Lomake, application: ImmutableLegacyApplicationWrapper, hakemus: HakemusLike)(implicit lang: Language.Language): Seq[AnswerId] = {
    val allOldAnswers = allAnswersFromApplication(application)
    val allNewAnswers = getAllAnswersForApplication(lomake, application, hakemus)

    val removedQuestions = AddedQuestionFinder.findAddedQuestions(lomake, allOldAnswers, allNewAnswers).toList
    removedQuestions.flatMap(_.answerIds)
  }

  def getAllAnswersForApplication(lomake: Lomake, application: ImmutableLegacyApplicationWrapper, hakemus: HakemusLike): Answers = {
    allAnswersFromApplication(application) ++
      updatedAnswersForHakuToiveet(lomake, application, hakemus.answers, hakemus.preferences) ++
      updatedAnswersForOtherPhases(application, hakemus.answers)
  }

  def allAnswersFromApplication(application: ImmutableLegacyApplicationWrapper) = {
    application.answers
  }

  private def updatedAnswersForOtherPhases(application: ImmutableLegacyApplicationWrapper, answers: Answers): Answers = {
    val allOtherPhaseAnswers = answers.filterKeys(phase => phase != preferencePhaseKey)
    allOtherPhaseAnswers.map { case (phase, answers) =>
      val existingAnswers = application.phaseAnswers(phase)
      (phase, existingAnswers ++ answers)
    }.toMap
  }

  private def updatedAnswersForHakuToiveet(lomake: Lomake, application: ImmutableLegacyApplicationWrapper, newAnswers: Hakemus.Answers, hakutoiveet: List[Hakemus.HakutoiveData]): Answers = {
    val previousAnswers = allAnswersFromApplication(application)

    val updatedAnswersForHakutoiveetPhase = HakutoiveetConverter.updateAnswers(hakutoiveet, newAnswers, previousAnswers)
    Map(preferencePhaseKey -> updatedAnswersForHakutoiveetPhase)
  }
}
