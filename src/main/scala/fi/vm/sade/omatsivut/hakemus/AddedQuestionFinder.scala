package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.domain.Hakemus._
import fi.vm.sade.omatsivut.domain._
import scala.collection.JavaConversions._

protected object AddedQuestionFinder {

  private def answersWithNewHakutoive(applicationSystem: ApplicationSystem, storedApplication: Application)(hakutoiveet: List[Hakutoive])(implicit lang: Language.Language) = {
    ApplicationUpdater.getAllUpdatedAnswersForApplicationWithHakutoiveet(applicationSystem)(storedApplication, hakutoiveet, storedApplication.getAnswers().toMap.mapValues(_.toMap))
  }

  def findQuestionsByNewHakutoive(applicationSystem: ApplicationSystem, storedApplication: Application, newHakemus: Hakemus, hakutoive: Hakutoive)(implicit lang: Language.Language): Set[QuestionLeafNode] = {
    val hakutoiveet = getOnlyAskedHakutoiveAsList(newHakemus, hakutoive)
    findAddedQuestions(applicationSystem, answersWithNewHakutoive(applicationSystem, storedApplication)(hakutoiveet), answersWithNewHakutoive(applicationSystem, storedApplication)(getOnlyAskedHakutoiveAsList(newHakemus, Map())))
  }

  def findQuestionsByUpdatedHakutoive(applicationSystem: ApplicationSystem, storedApplication: Application, newHakemus: Hakemus, hakutoive: Hakutoive)(implicit lang: Language.Language): Set[QuestionLeafNode] = {
    val hakutoiveet = getOnlyAskedHakutoiveAsList(newHakemus, hakutoive)
    findAddedQuestions(applicationSystem,
          ApplicationUpdater.getAllUpdatedAnswersForApplicationWithHakutoiveet(applicationSystem)(storedApplication, hakutoiveet, newHakemus.answers),
          answersWithNewHakutoive(applicationSystem, storedApplication)(hakutoiveet))
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
