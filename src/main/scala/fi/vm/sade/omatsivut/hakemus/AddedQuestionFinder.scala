package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.domain.Hakemus._
import fi.vm.sade.omatsivut.domain._

protected object AddedQuestionFinder {
  def findQuestionsByHakutoive(applicationSystem: ApplicationSystem, storedApplication: Application, newHakemus: Hakemus, existingHakutoiveet: List[Hakutoive], hakutoive: Hakutoive)(implicit lang: Language.Language): Set[QuestionLeafNode] = {
    def answersWithNewHakutoive(hakutoiveet: List[Hakutoive]) = {
      ApplicationUpdater.getAllUpdatedAnswersForApplicationWithHakutoiveet(applicationSystem)(storedApplication, hakutoiveet, Hakemus.emptyAnswers )
    }
    def answersWithCurrentAnswers(hakutoiveet: List[Hakutoive]) = {
      ApplicationUpdater.getAllUpdatedAnswersForApplicationWithHakutoiveet(applicationSystem)(storedApplication, hakutoiveet,
            Map(ApplicationUpdater.preferencePhaseKey  -> HakutoiveetConverter.convertToAnswers(hakutoiveet, newHakemus.answers)))
    }

    findAddedQuestions(applicationSystem, answersWithNewHakutoive(existingHakutoiveet ++ List(hakutoive)), answersWithNewHakutoive(existingHakutoiveet)) ++
    findAddedQuestions(applicationSystem, answersWithCurrentAnswers(existingHakutoiveet ++ List(hakutoive)), answersWithNewHakutoive(existingHakutoiveet ++ List(hakutoive)))
  }

  def findAddedQuestions(applicationSystem: ApplicationSystem, newAnswers: Answers, oldAnswers: Answers)(implicit lang: Language.Language): Set[QuestionLeafNode] = {
    val form = applicationSystem.getForm
    val oldAnswersFlat: Map[String, String] = HakemusConverter.flattenAnswers(oldAnswers)
    val newAnswersFlat: Map[String, String] = HakemusConverter.flattenAnswers(newAnswers)
    val oldQuestions = FormQuestionFinder.findQuestionsFromElements(Set(ElementWrapper.wrapFiltered(form, oldAnswersFlat)))
    val newQuestions = FormQuestionFinder.findQuestionsFromElements(Set(ElementWrapper.wrapFiltered(form, newAnswersFlat)))
    newQuestions.diff(oldQuestions)
  }
}
