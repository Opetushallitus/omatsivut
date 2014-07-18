package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.domain.elements.Element
import fi.vm.sade.omatsivut.domain.Hakemus._
import fi.vm.sade.omatsivut.domain.{QuestionGroup, QuestionNode}

import scala.collection.JavaConversions._

protected object AddedQuestionFinder {
  def findQuestionsByHakutoive(applicationSystem: ApplicationSystem, hakutoive: Hakutoive): Seq[QuestionNode] = {
    val answersWithNewHakutoive = Map(ApplicationUpdater.hakutoiveetPhase -> HakutoiveetConverter.convertToAnswers(List(hakutoive)))
    findAddedQuestions(applicationSystem, answersWithNewHakutoive, emptyAnswers)
  }

  def findAddedQuestions(applicationSystem: ApplicationSystem, newAnswers: Answers, oldAnswers: Answers): Seq[QuestionNode] = {
    val form = applicationSystem.getForm
    val addedElements = findAddedElements(form, newAnswers, oldAnswers)
    val addedQuestions = addedElements.flatMap { element =>
      FormQuestionFinder.findQuestions(form, element)
    }.groupBy { question => describePath(question.context.path)}.toList.map {
      case (groupTitle, questions) =>
        QuestionGroup(groupTitle, questions.toList)
    }
    addedQuestions.toList
  }

  def describePath(path: List[String]): String = path.mkString("", " - ", "")

  def findAddedElements(element: Element, newAnswers: Answers, oldAnswers: Answers, path: List[Element] = Nil): Set[Element] = {
    def flattenAnswers(answers: Map[String, Map[String, String]]): Map[String, String] = {
      answers.values.foldLeft(Map.empty.asInstanceOf[Map[String, String]]) { (a,b) => a ++ b }
    }
    val oldChildren = element.getChildren(flattenAnswers(oldAnswers))
    val newChildren = element.getChildren(flattenAnswers(newAnswers))
    val added = newChildren.filterNot { e => oldChildren.contains(e) }.toSet
    val existing = newChildren.union(oldChildren).toList
    added ++ existing.flatMap { element => findAddedElements(element, newAnswers, oldAnswers, (path ++ List(element))) }
  }

  private def descElement(el: Element) = el.getId
}
