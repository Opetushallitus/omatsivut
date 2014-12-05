package fi.vm.sade.omatsivut.lomake

import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus._
import fi.vm.sade.omatsivut.hakemus.{ImmutableLegacyApplicationWrapper, ApplicationUpdater, HakutoiveetConverter}
import ImmutableLegacyApplicationWrapper.wrap
import fi.vm.sade.omatsivut.lomake.domain.Lomake
import org.specs2.mutable.Specification

class AddedQuestionFinderSpec extends Specification {

  implicit val lang = Language.fi

  "RelatedQuestionHelper" should {
    "Report zero additional questions when not adding any answers" in {
      val addedQuestions = findAddedQuestions(Hakemus.emptyAnswers, Hakemus.emptyAnswers)
      addedQuestions.length must_== 0
    }

    "Report zero additional questions when keeping same answers" in {
      val addedQuestions = findAddedQuestions(answersWithNewHakutoive, answersWithNewHakutoive)
      addedQuestions.length must_== 0
    }

    "Report zero additional questions when re-ordering hakutoiveet" in {
      val answers1 = ApplicationUpdater.getAllUpdatedAnswersForApplication(lomake, wrap(applicationNivelKesa2013WithPeruskouluBaseEducationApp), hakemusMuutos.answers, hakemusMuutos.preferences)
      val answers2 = ApplicationUpdater.getAllUpdatedAnswersForApplication(lomake, wrap(applicationNivelKesa2013WithPeruskouluBaseEducationApp), hakemusMuutos.answers, hakemusMuutos.preferences.reverse)
      val addedQuestions = findAddedQuestions(answers1, answers2)
      val removedQuestions = findAddedQuestions(answers2, answers1)
      addedQuestions.length must_== 0
      removedQuestions.length must_== 0
    }

  }

  def findAddedQuestions(newAnswers: Answers, oldAnswers: Answers) = {
    AddedQuestionFinder.findAddedQuestions(lomake, newAnswers, oldAnswers).toList
  }

  val lomake = Lomake(applicationSystemNivelKesa2013)
  val answersWithNewHakutoive = Map(ApplicationUpdater.preferencePhaseKey -> HakutoiveetConverter.convertToAnswers(List(ammattistartti), Hakemus.emptyAnswers ))
}
