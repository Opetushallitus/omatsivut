package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.elements.Element
import fi.vm.sade.omatsivut.domain.Hakemus.Answers
import fi.vm.sade.omatsivut.hakemus.HakemusConverter.FlatAnswers

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

  def apply(element: Element, answers: FlatAnswers) = {
    FilteredElementWrapper(element, answers)
  }
}

case class SimpleElementWrapper(element: Element) extends ElementWrapper {
  import collection.JavaConversions._
  override def children = element.getChildren.toList.map(SimpleElementWrapper)
  def wrap(element: Element) = { SimpleElementWrapper(element) }
}

case class FilteredElementWrapper(element: Element, answers: FlatAnswers) extends ElementWrapper {
  import collection.JavaConversions._

  override def children = {
    element.getChildren(answers).toList.map(FilteredElementWrapper(_, answers))
  }

  def wrap(element: Element) = { FilteredElementWrapper(element, answers) }
}
