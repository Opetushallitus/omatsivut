package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.elements.Element
import fi.vm.sade.omatsivut.domain.Hakemus.Answers
import collection.JavaConversions._

object RelatedQuestionHelper {
  def findAddedElements(element: Element, newAnswers: Answers, oldAnswers: Answers): Seq[Element] = {
    def flattenAnswers(answers: Map[String, Map[String, String]]): Map[String, String] = {
      answers.values.foldLeft(Map.empty.asInstanceOf[Map[String, String]]) { (a,b) => a ++ b }
    }
    val oldChildren = element.getChildren(flattenAnswers(oldAnswers))
    val newChildren = element.getChildren(flattenAnswers(newAnswers))
    val added = newChildren.filterNot { e => oldChildren.contains(e) }
    val existing = newChildren.union(oldChildren).toList
    added ++ existing.flatMap { element => findAddedElements(element, newAnswers, oldAnswers) }
  }
}
