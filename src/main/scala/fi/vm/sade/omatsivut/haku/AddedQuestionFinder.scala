package fi.vm.sade.omatsivut.haku

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus._
import fi.vm.sade.omatsivut.hakemus.domain._
import fi.vm.sade.omatsivut.hakemus.{ApplicationUpdater, HakemusConverter, HakutoiveetConverter}
import fi.vm.sade.omatsivut.haku.domain.{QuestionGroup, QuestionLeafNode, QuestionNode}

object AddedQuestionFinder {
  val preferencePhaseKey = OppijaConstants.PHASE_APPLICATION_OPTIONS

  def findAddedQuestions(applicationSystem: ApplicationSystem, newAnswers: Answers, oldAnswers: Answers)(implicit lang: Language.Language): Set[QuestionLeafNode] = {
    val form = applicationSystem.getForm
    val oldAnswersFlat: Map[String, String] = HakemusConverter.flattenAnswers(oldAnswers)
    val oldQuestions = FormQuestionFinder.findQuestionsFromElements(Set(ElementWrapper.wrapFiltered(form, oldAnswersFlat)))
    val newAnswersFlat: Map[String, String] = HakemusConverter.flattenAnswers(newAnswers)
    val newQuestions = FormQuestionFinder.findQuestionsFromElements(Set(ElementWrapper.wrapFiltered(form, newAnswersFlat)))
    newQuestions.diff(oldQuestions)
  }

  private def getOnlyAskedHakutoiveAsList(newHakemus: HakemusMuutos, hakutoive: Hakutoive): List[Hakutoive] = {
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

  def findQuestions(applicationSystem: ApplicationSystem)(storedApplication: Application, hakemus: HakemusMuutos, newKoulutusIds: List[String])(implicit lang: Language.Language) = {
    val filteredForm: ElementWrapper = ElementWrapper.wrapFiltered(applicationSystem.getForm, HakemusConverter.flattenAnswers(ApplicationUpdater.getAllAnswersForApplication(applicationSystem, storedApplication.clone(), hakemus)))

    val questionsPerHakutoive: List[QuestionNode] = hakemus.hakutoiveet.zipWithIndex.flatMap { case (hakutoive, index) =>
      hakutoive.get("Koulutus-id") match {
        case Some(koulutusId) if (newKoulutusIds.contains(koulutusId)) =>
          val addedByHakutoive: Set[QuestionLeafNode] = AddedQuestionFinder.findQuestionsByHakutoive(applicationSystem, storedApplication, hakemus, hakutoive)
          val groupedQuestions: Seq[QuestionNode] = QuestionGrouper.groupQuestionsByStructure(filteredForm, addedByHakutoive)

          groupedQuestions match {
            case Nil => Nil
            case _ => List(QuestionGroup(HakutoiveetConverter.describe(hakutoive), groupedQuestions.toList))
          }
        case _ => Nil
      }
    }

    val duplicates = getDuplicateQuestions(questionsPerHakutoive) match {
      case questions: List[QuestionNode] if questions.nonEmpty => List(QuestionGroup("", QuestionGrouper.groupQuestionsByStructure(filteredForm, questions.toSet)))
      case _ => Nil
    }

    withoutDuplicates(questionsPerHakutoive) ::: duplicates
  }

  private def findQuestionsByHakutoive(applicationSystem: ApplicationSystem, storedApplication: Application, newHakemus: HakemusMuutos, hakutoive: Hakutoive)(implicit lang: Language.Language): Set[QuestionLeafNode] = {
    val onlyOneHakutoive = getOnlyAskedHakutoiveAsList(newHakemus, hakutoive)
    val currentAnswersWithOneHakutoive = ApplicationUpdater.getAllUpdatedAnswersForApplication(applicationSystem)(storedApplication, newHakemus.copy(hakutoiveet = onlyOneHakutoive))
    val noHakutoive = getOnlyAskedHakutoiveAsList(newHakemus, Map())
    val emptyAnswersWithNoHakutoive = ApplicationUpdater.getAllUpdatedAnswersForApplication(applicationSystem)(storedApplication, newHakemus.copy(hakutoiveet = noHakutoive).copy(answers = Hakemus.emptyAnswers))
    findAddedQuestions(applicationSystem, currentAnswersWithOneHakutoive, emptyAnswersWithNoHakutoive)
  }

  private def getDuplicateQuestions(questions: List[QuestionNode]) = {
    val n:List[QuestionLeafNode] = questions.flatMap(_.flatten)
    val groups = n.groupBy { node => node.id }
    groups.filter { case (_, nodes) => nodes.length > 1 }.map(_._2.head)
  }

  private def withoutDuplicates(questions: List[QuestionNode]) = {
    val duplicateIds = getDuplicateQuestions(questions).map(_.id).toSet

    questions.map {
      item => item match {
        case group: QuestionGroup =>
          group.filter { (question) => !duplicateIds.contains(question.id) }
        case _ =>
          item
      }
    }
  }
}
