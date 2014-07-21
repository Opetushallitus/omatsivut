package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.domain.Hakemus
import fi.vm.sade.omatsivut.domain.Hakemus._
import fi.vm.sade.omatsivut.hakemus.TestFixture._
import org.specs2.mutable.Specification

class AddedQuestionFinderSpec extends Specification {
  "RelatedQuestionHelper" should {
    "Report zero additional questions when not adding any answers" in {
      val addedQuestions = findAddedQuestions(Hakemus.emptyAnswers, Hakemus.emptyAnswers)
      addedQuestions.length must_== 0
    }

    "Find related questions when adding Hakutoive" in {
      var addedQuestions = findAddedQuestions(answersWithNewHakutoive, Hakemus.emptyAnswers)
      addedQuestions.length must_== 2
      addedQuestions = AddedQuestionFinder.findQuestionsByHakutoive(as, hakutoive)
      addedQuestions.length must_== 2
    }

    "Report zero additional questions when keeping same answers" in {
      val addedQuestions = findAddedQuestions(answersWithNewHakutoive, answersWithNewHakutoive)
      addedQuestions.length must_== 0
    }
  }

  def findAddedQuestions(newAnswers: Answers, oldAnswers: Answers) = {
    AddedQuestionFinder.findAddedQuestions(as, newAnswers, oldAnswers)
  }

  val as: ApplicationSystem = applicationSystem
  val answersWithNewHakutoive = Map(ApplicationUpdater.preferencePhaseKey -> HakutoiveetConverter.convertToAnswers(List(hakutoive)))
}
