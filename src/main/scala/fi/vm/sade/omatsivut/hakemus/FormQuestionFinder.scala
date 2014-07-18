package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.elements.custom.SocialSecurityNumber
import fi.vm.sade.haku.oppija.lomake.domain.elements.questions.{DropdownSelect, TextQuestion, CheckBox => HakuCheckBox, OptionQuestion => HakuOption, Radio => HakuRadio, TextArea => HakuTextArea}
import fi.vm.sade.haku.oppija.lomake.domain.elements.{Element, Titled, Phase, Theme, TitledGroup}
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.domain._

import scala.collection.JavaConversions._

protected object FormQuestionFinder extends Logging {
  def findQuestions(contextElement: Element, elementsToScan: Set[Element]): List[QuestionGroup] = {
    elementsToScan.flatMap { element =>
      getElementsOfType[Titled](element).flatMap { titled =>
        titledElementToQuestions(contextElement, titled)
      }
    }.groupBy { case (question, elementContext) => elementContext.namedParents}.toList.map {
      case (parents, questions) =>
        val lang = "fi" // TODO: kieliversiot
        val groupNamePath = parents.map(_.getI18nText.getTranslations.get(lang))
        val groupName = groupNamePath.mkString("", " - ", "")

        QuestionGroup(groupName, questions.map(_._1).toList)
    }
  }

  private def getImmediateChildElementsOfType[A](rootElement: Element)(implicit mf : Manifest[A]): List[A] = {
    rootElement.getChildren.toList.flatMap { child =>
      if (mf.runtimeClass.isAssignableFrom(child.getClass)) {
        List(child.asInstanceOf[A])
      } else {
        Nil
      }
    }
  }

  private def getChildElementsOfType[A](rootElement: Element)(implicit mf : Manifest[A]): List[A] = {
    rootElement.getChildren.toList.flatMap { child => getElementsOfType(child)}
  }

  private def getElementsOfType[A](rootElement: Element, includeRoot: Boolean = false)(implicit mf : Manifest[A]): List[A] = {
    def convertChildElements(element: Element): List[A] = {
      element.getChildren.toList.flatMap { child => getElementsOfType(child)}
    }
    if (mf.runtimeClass.isAssignableFrom(rootElement.getClass)) {
      rootElement.asInstanceOf[A] :: convertChildElements(rootElement)
    } else {
      convertChildElements(rootElement)
    }
  }

  private def options(e: HakuOption): List[Choice] = {
    e.getOptions.map(o => Choice(title(o), o.getValue, o.isDefaultOption)).toList
  }
  private def options(e: TitledGroup): List[Choice] = {
    getChildElementsOfType[HakuCheckBox](e).map(o => Choice(title(o), o.getId()))
  }
  private def title[T <: Titled](e: T): String = {
    e.getI18nText.getTranslations.get("fi") // TODO: kieliversiot
  }
  private def helpText[T <: Titled](e: T): String = {
    val help = e.getVerboseHelp()
    if (help == null)
      ""
    else
      e.getVerboseHelp().getTranslations.get("fi") // TODO: kieliversiot
  }

  private def titledElementToQuestions(contextElement: Element, element: Titled): List[(Question, ElementContext)] = {
    val elementContext = new ElementContext(contextElement, element)
    def id = QuestionId(elementContext.phase.getId, element.getId)
    def containsCheckBoxes(e: TitledGroup): Boolean = {
      getImmediateChildElementsOfType[HakuCheckBox](e).nonEmpty
    }

    (element match {
      case e: TextQuestion => List(Text(id, title(e), helpText(e)))
      case e: HakuTextArea => List(TextArea(id, title(e), helpText(e)))
      case e: HakuRadio => List(Radio(id, title(e), helpText(e), options(e)))
      case e: DropdownSelect => List(Dropdown(id, title(e), helpText(e), options(e)))
      case e: TitledGroup if containsCheckBoxes(e) => List(Checkbox(id, title(e), helpText(e), options(e)))
      case e: SocialSecurityNumber => List(Text(id, title(e), helpText(e))) // Should never happen in prod
      case _ => {
        logger.error("Could not convert element of type: " + element.getType)
        Nil
      }
    }).map { question => (question, elementContext)}
  }
}

class ElementContext(val contextElement: Element, val element: Element) {
  lazy val parentsFromRootDown: List[Element] = {
    def findParents(e: Element, r: Element): Option[List[Element]] = {
      if (e == r) {
        Some(Nil)
      } else {
        val x: List[List[Element]] = r.getChildren.toList flatMap { child: Element =>
          findParents(e, child).toList.map { path => r :: path }
        }
        x.headOption
      }
    }
    findParents(element, contextElement).get
  }

  lazy val phase: Phase = byType[Phase](parentsFromRootDown).head

  lazy val namedParents: List[Titled] = {
    def title(e: Element): List[Titled] = {
      e match {
        case e: Titled if (e.isInstanceOf[Phase] || e.isInstanceOf[Theme]) && (e.getI18nText != null && e.getI18nText.getTranslations != null) =>
          List(e)
        case _ => Nil
      }
    }
    parentsFromRootDown.tail.flatMap(title).distinct
  }

  private def byType[T](xs: List[AnyRef])(implicit mf: Manifest[T]): List[T] = {
    xs.flatMap {
      case p: T => List(p)
      case _ => Nil
    }
  }
}