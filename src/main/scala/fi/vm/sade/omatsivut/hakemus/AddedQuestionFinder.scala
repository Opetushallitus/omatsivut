package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.domain.Hakemus._
import fi.vm.sade.omatsivut.domain._

protected object AddedQuestionFinder {
  def findQuestionsByHakutoive(applicationSystem: ApplicationSystem, application: Application, existingHakutoiveet: List[Hakutoive], hakutoive: Hakutoive): Set[QuestionLeafNode] = {
    def answersWith(hakutoiveet: List[Hakutoive]) = {
      ApplicationUpdater.getAllUpdatedAnswersForApplicationWithHakutoiveet(applicationSystem)(application, hakutoiveet)
    }
    findAddedQuestions(applicationSystem, answersWith(existingHakutoiveet ++ List(hakutoive)), answersWith(existingHakutoiveet))
  }

  def findAddedQuestions(applicationSystem: ApplicationSystem, newAnswers: Answers, oldAnswers: Answers): Set[QuestionLeafNode] = {
    val form = applicationSystem.getForm
    val oldAnswersFlat: Map[String, String] = HakemusConverter.flattenAnswers(oldAnswers)
    val newAnswersFlat: Map[String, String] = HakemusConverter.flattenAnswers(newAnswers)
    val oldQuestions = FormQuestionFinder.findQuestionsFromElements(Set(ElementWrapper.wrapFiltered(form, oldAnswersFlat)))
    val newQuestions = FormQuestionFinder.findQuestionsFromElements(Set(ElementWrapper.wrapFiltered(form, newAnswersFlat)))
    newQuestions.diff(oldQuestions)
  }
}
