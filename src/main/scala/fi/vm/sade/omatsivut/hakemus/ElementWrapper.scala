package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.elements._
import fi.vm.sade.omatsivut.domain.Hakemus.Answers
import fi.vm.sade.omatsivut.hakemus.HakemusConverter.FlatAnswers

trait ElementWrapper {
  def element: Element
  def children: List[ElementWrapper]
  def parent: Option[ElementWrapper]
  def id = element.getId
  
  def findById(idToLookFor: String):Option[ElementWrapper] = {
    if (id == idToLookFor) {
      Some(this)
    } else {
      for (child <- children) {
        val found = child.findById(idToLookFor)
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


  lazy val parentsFromRootDown: List[Element] = {
    def findParents(e: ElementWrapper): List[ElementWrapper] = {
      e.parent match {
        case None => Nil
        case Some(parent) => findParents(parent) ++ List(parent)
      }
    }

    findParents(this).map(_.element)
  }

  lazy val phase: Phase = byType[Phase](parentsFromRootDown).head

  lazy val namedParents: List[Titled] = {
    parentsFromRootDown.flatMap {
      case e: Form => List(e)
      case e: Phase => List(e)
      case e: Theme => List(e)
      case _ => Nil
    }
  }

  def selfAndParents: List[Element] = {
    parentsFromRootDown ++ List(element)
  }


  def getChildElementsOfType[A](implicit mf : Manifest[A]): List[ElementWrapper] = {
    children.toList.flatMap { child => child.getElementsOfType}
  }

  def getElementsOfType[A](implicit mf : Manifest[A]): List[ElementWrapper] = {
    def convertChildElements(element: ElementWrapper): List[ElementWrapper] = {
      element.children.flatMap { child => child.getElementsOfType}
    }
    if (mf.runtimeClass.isAssignableFrom(element.getClass)) {
      this :: convertChildElements(this)
    } else {
      convertChildElements(this)
    }
  }

  private def byType[T](xs: List[AnyRef])(implicit mf: Manifest[T]): List[T] = {
    xs.flatMap {
      case p: T => List(p)
      case _ => Nil
    }
  }
}

object ElementWrapper {
  def wrapFiltered(element: Element, answers: FlatAnswers) = {
    new FilteredElementWrapper(element, None, answers)
  }
}


class FilteredElementWrapper(val element: Element, val parent: Option[ElementWrapper], answers: FlatAnswers) extends ElementWrapper {
  import collection.JavaConversions._

  override lazy val children = {
    element.getChildren(answers).toList.map(new FilteredElementWrapper(_, Some(this), answers))
  }
}
