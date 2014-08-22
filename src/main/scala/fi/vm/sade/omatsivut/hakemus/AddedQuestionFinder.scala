package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.domain.Hakemus._
import fi.vm.sade.omatsivut.domain._
import scala.collection.JavaConversions._

protected object AddedQuestionFinder {

  def findQuestionsByHakutoive(applicationSystem: ApplicationSystem, storedApplication: Application, newHakemus: Hakemus, hakutoive: Hakutoive)(implicit lang: Language.Language): Set[QuestionLeafNode] = {
    val onlyOneHakutoive = getOnlyAskedHakutoiveAsList(newHakemus, hakutoive)
    val currentAnswersWithOneHakutoive = ApplicationUpdater.getAllUpdatedAnswersForApplication(applicationSystem)(storedApplication, newHakemus.copy(hakutoiveet = onlyOneHakutoive))
    val noHakutoive = getOnlyAskedHakutoiveAsList(newHakemus, Map())
    val currentAnswersWithNoHakutoive = ApplicationUpdater.getAllUpdatedAnswersForApplication(applicationSystem)(storedApplication, newHakemus.copy(hakutoiveet = noHakutoive))
    findAddedQuestions(applicationSystem, currentAnswersWithOneHakutoive, currentAnswersWithNoHakutoive)
  }

  def findAddedQuestions(applicationSystem: ApplicationSystem, newAnswers: Answers, oldAnswers: Answers)(implicit lang: Language.Language): Set[QuestionLeafNode] = {
    val form = applicationSystem.getForm
    val oldAnswersFlat: Map[String, String] = HakemusConverter.flattenAnswers(oldAnswers)
    val newAnswersFlat: Map[String, String] = HakemusConverter.flattenAnswers(newAnswers)
    val oldQuestions = FormQuestionFinder.findQuestionsFromElements(Set(ElementWrapper.wrapFiltered(form, oldAnswersFlat)))
    val newQuestions = FormQuestionFinder.findQuestionsFromElements(Set(ElementWrapper.wrapFiltered(form, newAnswersFlat)))
    newQuestions.diff(oldQuestions)
  }

  def getOnlyAskedHakutoiveAsList(newHakemus: Hakemus, hakutoive: Hakutoive): List[Hakutoive] = {
    def getHakutoive(listItem: Hakutoive): Hakutoive = {
      if(listItem == hakutoive) {
        hakutoive
      }
      else {
        Map()
      }
    }
    newHakemus.hakutoiveet.map(getHakutoive)
  }
}
