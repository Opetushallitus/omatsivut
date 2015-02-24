package fi.vm.sade.omatsivut.lomake

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.Answers
import fi.vm.sade.hakemuseditori.hakemus.{ImmutableLegacyApplicationWrapper, AnswerHelper, HakutoiveetConverter}
import fi.vm.sade.hakemuseditori.lomake.AddedQuestionFinder
import fi.vm.sade.hakemuseditori.lomake.domain.Lomake
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
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
      val answers1 = AnswerHelper.getAllUpdatedAnswersForApplication(lomake, ImmutableLegacyApplicationWrapper.wrap(applicationNivelKesa2013WithPeruskouluBaseEducationApp), hakemusMuutos.answers, hakemusMuutos.preferences)
      val answers2 = AnswerHelper.getAllUpdatedAnswersForApplication(lomake, ImmutableLegacyApplicationWrapper.wrap(applicationNivelKesa2013WithPeruskouluBaseEducationApp), hakemusMuutos.answers, hakemusMuutos.preferences.reverse)
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
  val answersWithNewHakutoive = Map(AnswerHelper.preferencePhaseKey -> HakutoiveetConverter.convertToAnswers(List(ammattistartti), Hakemus.emptyAnswers ))
}
