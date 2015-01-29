package fi.vm.sade.hakemuseditori.lomake

import fi.vm.sade.haku.oppija.lomake.domain.I18nText
import fi.vm.sade.haku.oppija.lomake.domain.elements._
import fi.vm.sade.haku.oppija.lomake.domain.elements.custom.gradegrid.GradeGridOptionQuestion
import fi.vm.sade.haku.oppija.lomake.domain.elements.questions.OptionQuestion
import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.hakemus.FlatAnswers.FlatAnswers
import scala.collection.JavaConversions._

class OptionWrapper(element: ElementWrapper) {
  def value: String = element.element.asInstanceOf[questions.Option].getValue
  def title(implicit lang: Language) = element.title
}

trait ElementWrapper {
  def element: Element
  def children: List[ElementWrapper]
  lazy val options: List[OptionWrapper] = element match {
    case e: OptionQuestion => e.getOptions.toList.map{ option => new OptionWrapper(wrap(option)) }
    case e: GradeGridOptionQuestion => e.getOptions.toList.map{ option => new OptionWrapper(wrap(option))}
  }
  def parent: Option[ElementWrapper]
  def id = element.getId
  def title(implicit lang: Language) = {
    ElementWrapper.t(element.asInstanceOf[Titled].getI18nText)
  }

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

  lazy val phase: Option[Phase] = byType[Phase](parentsFromRootDown).headOption

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

  protected def wrap(element: Element): ElementWrapper

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
  def wrapUnfiltered(element: Element) = {
    new UnfilteredElementWrapper(element, None)
  }

  def t(text: I18nText)(implicit lang: Language) = text match {
    case null => ""
    case text => text.getTranslations.get(lang.toString) match {
      case null => ""
      case t => t
    }
  }
}


class FilteredElementWrapper(val element: Element, val parent: Option[ElementWrapper], answers: FlatAnswers) extends ElementWrapper {
  import scala.collection.JavaConversions._

  override lazy val children = {
    element.getChildren(answers).toList.map(new FilteredElementWrapper(_, Some(this), answers))
  }

  override protected def wrap(element: Element) = {
    new FilteredElementWrapper(element, Some(this), answers)
  }
}

class UnfilteredElementWrapper(val element: Element, val parent: Option[ElementWrapper]) extends ElementWrapper {
  import scala.collection.JavaConversions._

  override lazy val children = {
    element.getChildren.toList.map(new UnfilteredElementWrapper(_, Some(this)))
  }

  override protected def wrap(element: Element) = {
    new UnfilteredElementWrapper(element, Some(this))
  }
}
