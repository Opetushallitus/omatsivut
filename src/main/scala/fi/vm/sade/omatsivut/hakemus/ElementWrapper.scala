package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.elements.Element
import fi.vm.sade.omatsivut.domain.Hakemus.Answers

trait ElementWrapper {
  def element: Element
  def children: List[ElementWrapper]
  def findById(id: String):Option[ElementWrapper] = {
    if (element.getId == id) {
      Some(this)
    } else {
      for (child <- children) {
        val found = child.findById(id)
        found match {
          case Some(el) => return Some(el)
          case _ =>
        }
      }
      None
    }
  }
  def findAllById(id: String):List[ElementWrapper] = {
    val forChildren = children.flatMap { child => child.findAllById(id) }
    if (element.getId == id) {
      this :: forChildren
    } else {
      forChildren
    }
  }
  def wrap(element: Element) : ElementWrapper
}

object ElementWrapper {
  def apply(element: Element) = {
    SimpleElementWrapper(element)
  }

  def apply(element: Element, answers: Answers) = {
    FilteredElementWrapper(element, answers)
  }
}

case class SimpleElementWrapper(element: Element) extends ElementWrapper {
  import collection.JavaConversions._
  override def children = element.getChildren.toList.map(SimpleElementWrapper)
  def wrap(element: Element) = { SimpleElementWrapper(element) }
}

case class FilteredElementWrapper(element: Element, answers: Answers) extends ElementWrapper {
  import collection.JavaConversions._

  override def children = {
    element.getChildren(flattenAnswers(answers)).toList.map(FilteredElementWrapper(_, answers))
  }

  private def flattenAnswers(answers: Map[String, Map[String, String]]): Map[String, String] = {
    answers.values.foldLeft(Map.empty.asInstanceOf[Map[String, String]]) { (a,b) => a ++ b }
  }

  def wrap(element: Element) = { FilteredElementWrapper(element, answers) }
}
