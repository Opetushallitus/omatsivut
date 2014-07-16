package fi.vm.sade.omatsivut

import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.domain.Hakemus
import fi.vm.sade.omatsivut.hakemus.{HakuRepository, RelatedQuestionHelper}
import org.specs2.mutable.Specification

class RelatedQuestionsSpec extends Specification {

  def getFixtureApplicationSystem: ApplicationSystem = {
    (new AppConfig.IT).withConfig { appConfig =>
      appConfig.springContext.applicationSystemService.getApplicationSystem("1.2.246.562.5.2014022711042555034240")
    }
  }

  "RelatedQuestionHelper" should {
    "Find related questions when adding Hakutoive" in {
      val as: ApplicationSystem = getFixtureApplicationSystem
      val oldAnswers = Hakemus.emptyAnswers
      val newAnswers = Hakemus.emptyAnswers
      val addedElements = RelatedQuestionHelper.findAddedElements(as.getForm, newAnswers, oldAnswers)
      addedElements.length must_== 0
    }
  }
}
