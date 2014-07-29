package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.domain.elements.{Form, Element}
import fi.vm.sade.omatsivut.domain.Hakemus._
import fi.vm.sade.omatsivut.domain._

import scala.collection.JavaConversions._

protected object AddedQuestionFinder {
  def findQuestionsByHakutoive(applicationSystem: ApplicationSystem, application: Application, existingHakutoiveet: List[Hakutoive], hakutoive: Hakutoive): Set[QuestionLeafNode] = {
    def answersWith(hakutoiveet: List[Hakutoive]) = {
      ApplicationUpdater.getAllUpdatedAnswersForApplicationWithHakutoiveet(applicationSystem)(application, hakutoiveet)
    }
    findAddedQuestions(applicationSystem, answersWith(existingHakutoiveet ++ List(hakutoive)), answersWith(existingHakutoiveet))
  }

  def findAddedQuestions(applicationSystem: ApplicationSystem, newAnswers: Answers, oldAnswers: Answers): Set[QuestionLeafNode] = {
    val form = applicationSystem.getForm
    val oldAnswersFlat: Map[String, String] = flattenAnswers(oldAnswers)
    val newAnswersFlat: Map[String, String] = flattenAnswers(newAnswers)
    val addedElements = findAddedElements(form, newAnswersFlat, oldAnswersFlat)
    FormQuestionFinder.findQuestionsFromElements(ElementWrapper(form, newAnswers), addedElements).filter { question =>
      !containsElementId(question.id.questionId, oldAnswersFlat, form)
    }
  }

  private def containsElementId(id: String, contextAnswers: FlatAnswers, context: Element): Boolean = {
    if (context.getId == id) {
      true
    } else {
      getChildrenWithAnswers(context, contextAnswers).find { child: Element =>
        containsElementId(id, contextAnswers, child)
      }.isDefined
    }
  }

  private def getChildrenWithAnswers(element: Element, answers: FlatAnswers) = {
    element.getChildren(answers).toList
  }

  private def flattenAnswers(answers: Map[String, Map[String, String]]): Map[String, String] = {
    answers.values.foldLeft(Map.empty.asInstanceOf[Map[String, String]]) { (a,b) => a ++ b }
  }

  private def findAddedElements(element: Element, newAnswers: FlatAnswers, oldAnswers: FlatAnswers, path: List[Element] = Nil): Set[Element] = {
    // TODO: performance hotspot
    val oldChildren = getChildrenWithAnswers(element, oldAnswers)
    val newChildren = getChildrenWithAnswers(element, newAnswers)
    val added = newChildren.filterNot { e => oldChildren.contains(e) }.toSet
    val existing = newChildren.union(oldChildren).toList
    added ++ existing.flatMap { element => findAddedElements(element, newAnswers, oldAnswers, (path ++ List(element))) }
  }

  private def descElement(el: Element) = el.getId

  type FlatAnswers = Map[String, String]
}
