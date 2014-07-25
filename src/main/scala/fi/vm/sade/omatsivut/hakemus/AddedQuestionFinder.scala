package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.domain.elements.{Form, Element}
import fi.vm.sade.omatsivut.domain.Hakemus._
import fi.vm.sade.omatsivut.domain.{Question, QuestionGroup, QuestionNode}

import scala.collection.JavaConversions._

protected object AddedQuestionFinder {
  def findQuestionsByHakutoive(applicationSystem: ApplicationSystem, existingHakutoiveet: List[Hakutoive], hakutoive: Hakutoive): Seq[QuestionNode] = {
    def answersWith(hakutoiveet: List[Hakutoive]) = Map(ApplicationUpdater.preferencePhaseKey -> HakutoiveetConverter.convertToAnswers(hakutoiveet))
    findAddedQuestions(applicationSystem, answersWith(existingHakutoiveet ++ List(hakutoive)), answersWith(existingHakutoiveet))
  }

  def findAddedQuestions(applicationSystem: ApplicationSystem, newAnswers: Answers, oldAnswers: Answers): Seq[QuestionNode] = {
    val form = applicationSystem.getForm
    val addedElements = findAddedElements(form, newAnswers, oldAnswers)
    val root = QuestionGroup("root", FormQuestionFinder.findQuestions(form, addedElements))
      .filter({ question: Question =>
        !containsElementId(question.id.questionId, oldAnswers, form)
      })
    root.questions
  }

  private def containsElementId(id: String, contextAnswers: Answers, context: Element): Boolean = {
    if (context.getId == id) {
      true
    } else {
      getChildrenWithAnswers(context, contextAnswers).find { child: Element =>
        containsElementId(id, contextAnswers, child)
      }.isDefined
    }
  }

  private def getChildrenWithAnswers(element: Element, answers: Answers) = {
    element.getChildren(flattenAnswers(answers)).toList
  }

  private def flattenAnswers(answers: Map[String, Map[String, String]]): Map[String, String] = {
    answers.values.foldLeft(Map.empty.asInstanceOf[Map[String, String]]) { (a,b) => a ++ b }
  }

  private def findAddedElements(element: Element, newAnswers: Answers, oldAnswers: Answers, path: List[Element] = Nil): Set[Element] = {
    val oldChildren = getChildrenWithAnswers(element, oldAnswers)
    val newChildren = getChildrenWithAnswers(element, newAnswers)
    val added = newChildren.filterNot { e => oldChildren.contains(e) }.toSet
    val existing = newChildren.union(oldChildren).toList
    added ++ existing.flatMap { element => findAddedElements(element, newAnswers, oldAnswers, (path ++ List(element))) }
  }

  private def descElement(el: Element) = el.getId
}
