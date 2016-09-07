package fi.vm.sade.hakemuseditori.lomake

import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus._
import fi.vm.sade.hakemuseditori.hakemus.domain._
import fi.vm.sade.hakemuseditori.hakemus.{AnswerHelper, FlatAnswers, HakutoiveetConverter, ImmutableLegacyApplicationWrapper}
import fi.vm.sade.hakemuseditori.lomake.domain.{Lomake, QuestionGroup, QuestionLeafNode, QuestionNode}

object AddedQuestionFinder {
  val preferencePhaseKey = OppijaConstants.PHASE_APPLICATION_OPTIONS

  def findAddedQuestions(lomake: Lomake, newAnswers: Answers, oldAnswers: Answers)(implicit lang: Language.Language): Set[QuestionLeafNode] = {
    val form = lomake.form
    val oldAnswersFlat: Map[String, String] = FlatAnswers.flatten(oldAnswers)
    val oldQuestions = FormQuestionFinder.findQuestionsFromElements(Set(ElementWrapper.wrapFiltered(form, oldAnswersFlat)), oldAnswersFlat)
    val newAnswersFlat: Map[String, String] = FlatAnswers.flatten(newAnswers)
    val newQuestions = FormQuestionFinder.findQuestionsFromElements(Set(ElementWrapper.wrapFiltered(form, newAnswersFlat)), newAnswersFlat)
    newQuestions.diff(oldQuestions)
  }

  def findQuestions(applicationSystem: Lomake)(storedApplication: ImmutableLegacyApplicationWrapper, hakemusMuutos: HakemusLike, hakutoiveet: List[String])(implicit lang: Language.Language): List[QuestionNode] = {
    val filteredForm: ElementWrapper = ElementWrapper.wrapFiltered(applicationSystem.form, FlatAnswers.flatten(AnswerHelper.getAllAnswersForApplication(applicationSystem, storedApplication, hakemusMuutos)))

    val questionsPerHakutoive: List[QuestionNode] = hakemusMuutos.preferences.flatMap(hakutoive =>
      hakutoive.get("Koulutus-id") match {
        case Some(koulutusId) if hakutoiveet.contains(koulutusId) =>
          val addedByHakutoive: Set[QuestionLeafNode] = findQuestionsByHakutoive(applicationSystem, storedApplication, hakemusMuutos, hakutoive)
          val groupedQuestions: Seq[QuestionNode] = QuestionGrouper.groupQuestionsByStructure(filteredForm, addedByHakutoive)

          groupedQuestions match {
            case Nil => Nil
            case _ => List(QuestionGroup(HakutoiveetConverter.describe(hakutoive), groupedQuestions.toList))
          }
        case _ => Nil
      }
    )

    val duplicates = getDuplicateQuestions(questionsPerHakutoive) match {
      case questions: List[QuestionNode] if questions.nonEmpty => List(QuestionGroup("", QuestionGrouper.groupQuestionsByStructure(filteredForm, questions.toSet)))
      case _ => Nil
    }

    withoutDuplicates(questionsPerHakutoive) ::: duplicates
  }

  private def findQuestionsByHakutoive(lomake: Lomake, storedApplication: ImmutableLegacyApplicationWrapper, hakemus: HakemusLike, hakutoive: HakutoiveData)(implicit lang: Language.Language): Set[QuestionLeafNode] = {
    val onlyOneHakutoive = removeAllOtherHakutoive(hakemus, hakutoive)
    val currentAnswersWithOneHakutoive = AnswerHelper.getAllUpdatedAnswersForApplication(lomake, storedApplication, hakemus.answers, onlyOneHakutoive)
    val noHakutoive = removeAllOtherHakutoive(hakemus, Map())
    val emptyAnswersWithNoHakutoive = AnswerHelper.getAllUpdatedAnswersForApplication(lomake, storedApplication, Hakemus.emptyAnswers, noHakutoive)
    findAddedQuestions(lomake, currentAnswersWithOneHakutoive, emptyAnswersWithNoHakutoive)
  }

  private def removeAllOtherHakutoive(hakemus: HakemusLike, hakutoive: HakutoiveData): List[HakutoiveData] = {
    def getHakutoive(listItem: HakutoiveData): HakutoiveData = {
      if(listItem == hakutoive) {
        hakutoive
      }
      else {
        Map()
      }
    }
    hakemus.preferences.map(getHakutoive)
  }

  private def getDuplicateQuestions(questions: List[QuestionNode]) = {
    val n:List[QuestionLeafNode] = questions.flatMap(_.flatten)
    val groups = n.groupBy { node => node.id }
    groups.filter { case (_, nodes) => nodes.length > 1 }.map(_._2.head)
  }

  private def withoutDuplicates(questions: List[QuestionNode]) = {
    val duplicateIds = getDuplicateQuestions(questions).map(_.id).toSet

    questions.flatMap {
      item => item match {
        case group: QuestionGroup => {
          val filteredGroup = group.filter { (question) => !duplicateIds.contains(question.id) }
          if(filteredGroup.questions.isEmpty) {
            None
          }
          else {
            Some(filteredGroup)
          }
        }
        case _ =>
          Some(item)
      }
    }
  }
}
