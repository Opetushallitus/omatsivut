package fi.vm.sade.omatsivut

import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.domain.Hakemus
import fi.vm.sade.omatsivut.domain.Hakemus._
import fi.vm.sade.omatsivut.fixtures.JsonFixtureMaps
import fi.vm.sade.omatsivut.hakemus.{ApplicationUpdater, HakuRepository, RelatedQuestionHelper}
import org.specs2.mutable.Specification

class RelatedQuestionsSpec extends Specification {
  "RelatedQuestionHelper" should {
    "Report zero additional questions when not adding any answers" in {
      val addedQuestions = findAddedQuestions(Hakemus.emptyAnswers, Hakemus.emptyAnswers)
      addedQuestions.length must_== 0
    }

    "Find related questions when adding Hakutoive" in {
      var addedQuestions = findAddedQuestions(answersWithNewHakutoive, Hakemus.emptyAnswers)
      addedQuestions.length must_== 5
      addedQuestions = RelatedQuestionHelper.findQuestionsByHakutoive(as, hakutoive)
      addedQuestions.length must_== 5
    }

    "Report zero additional questions when keeping same answers" in {
      val addedQuestions = findAddedQuestions(answersWithNewHakutoive, answersWithNewHakutoive)
      addedQuestions.length must_== 0
    }
  }

  def findAddedQuestions(newAnswers: Answers, oldAnswers: Answers) = {
    RelatedQuestionHelper.findAddedQuestions(as, newAnswers, oldAnswers)
  }

  def getFixtureApplicationSystem: ApplicationSystem = {
    (new AppConfig.IT).withConfig { appConfig =>
      appConfig.springContext.applicationSystemService.getApplicationSystem("1.2.246.562.5.2014022711042555034240")
    }
  }
  val as: ApplicationSystem = getFixtureApplicationSystem
  val hakutoive: Hakutoive = JsonFixtureMaps.find[Hakutoive]("/mockdata/hakutoiveet.json", "1.2.246.562.14.2014032812530780195965")
  val answersWithNewHakutoive = Map(ApplicationUpdater.hakutoiveetPhase -> ApplicationUpdater.convertHakutoiveet(List(hakutoive)))
}
