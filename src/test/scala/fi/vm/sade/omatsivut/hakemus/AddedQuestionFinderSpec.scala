package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.domain.Hakemus
import fi.vm.sade.omatsivut.domain.Hakemus._
import fi.vm.sade.omatsivut.fixtures.TestFixture
import TestFixture._
import org.specs2.mutable.Specification

class AddedQuestionFinderSpec extends Specification {
  "RelatedQuestionHelper" should {
    "Report zero additional questions when not adding any answers" in {
      val addedQuestions = findAddedQuestions(Hakemus.emptyAnswers, Hakemus.emptyAnswers)
      addedQuestions.length must_== 0
    }

    "Find related questions when adding Hakutoive" in {
      var addedQuestions = findAddedQuestions(answersWithNewHakutoive, Hakemus.emptyAnswers)
      addedQuestions.length must_== 11
      addedQuestions = AddedQuestionFinder.findQuestionsByHakutoive(as, Nil, ammattistartti).toList
      addedQuestions.length must_== 11
    }

    "Report zero additional questions when keeping same answers" in {
      val addedQuestions = findAddedQuestions(answersWithNewHakutoive, answersWithNewHakutoive)
      addedQuestions.length must_== 0
    }

    "Report zero additional questions when re-ordering hakutoiveet" in {
      val answers1 = ApplicationUpdater.getAllUpdatedAnswersForApplication(as)(application, hakemus)
      val answers2 = ApplicationUpdater.getAllUpdatedAnswersForApplication(as)(application, hakemus.copy(
        hakutoiveet = hakemus.hakutoiveet.reverse
      ))
      val addedQuestions = findAddedQuestions(answers1, answers2)
      val removedQuestions = findAddedQuestions(answers2, answers1)
      addedQuestions.length must_== 0
      removedQuestions.length must_== 0
    }

  }

  def findAddedQuestions(newAnswers: Answers, oldAnswers: Answers) = {
    AddedQuestionFinder.findAddedQuestions(as, newAnswers, oldAnswers).toList
  }

  val as: ApplicationSystem = applicationSystem
  val answersWithNewHakutoive = Map(ApplicationUpdater.preferencePhaseKey -> HakutoiveetConverter.convertToAnswers(List(ammattistartti)))
}
