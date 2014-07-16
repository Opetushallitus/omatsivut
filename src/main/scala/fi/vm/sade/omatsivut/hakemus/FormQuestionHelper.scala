package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.domain.elements.custom.SocialSecurityNumber
import fi.vm.sade.haku.oppija.lomake.domain.elements._
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.domain.Text
import fi.vm.sade.omatsivut.domain._
import fi.vm.sade.haku.oppija.lomake.domain.elements.questions.{DropdownSelect, TextQuestion, OptionQuestion => HakuOption, Radio => HakuRadio, TextArea => HakuTextArea, CheckBox => HakuCheckBox}
import collection.JavaConversions._

object FormQuestionHelper extends Logging {
  def questionsByIds(applicationSystem: ApplicationSystem, ids: Seq[String]): List[Question] = {
    findQuestions(applicationSystem, { titledElement => ids.contains(titledElement.getId)})
  }
  def findQuestions(applicationSystem: ApplicationSystem, filterFn: (Titled => Boolean)): List[Question] = {
    getPhases(applicationSystem).flatMap { phase =>
      getElementsOfType[Titled](phase)
        .filter(filterFn)
        .flatMap { titled =>
          titledElementToQuestions(phase, titled)
        }
    }
  }
  def findQuestions(contextElement: Element, element: Element): List[Question] = {
    getElementsOfType[Titled](element).flatMap { titled =>
      titledElementToQuestions(contextElement, titled)
    }
  }

  def getPhases(applicationSystem: ApplicationSystem) = {
    getChildElementsOfType[Phase](applicationSystem.getForm)
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
    getChildElementsOfType[HakuCheckBox](e).map(o => Choice(title(o), o.getValue))
  }
  private def title[T <: Titled](e: T): Translations = {
    Translations(e.getI18nText.getTranslations.toMap)
  }

  private def titledElementToQuestions(contextElement: Element, element: Titled): List[Question] = {
    val elementContext = new ElementContext(contextElement, element)
    def id = QuestionId(elementContext.phase.getId, element.getId)
    def containsCheckBoxes(e: TitledGroup): Boolean = {
      getChildElementsOfType[HakuCheckBox](e).nonEmpty
    }
    def ctx = {
      QuestionContext(elementContext.namedParentPath)
    }

    element match {
      case e: TextQuestion => List(Text(ctx, id, title(e)))
      case e: HakuTextArea => List(TextArea(ctx, id, title(e)))
      case e: HakuRadio => List(Radio(ctx, id, title(e), options(e)))
      case e: DropdownSelect => List(Dropdown(ctx, id, title(e), options(e)))
      case e: TitledGroup if containsCheckBoxes(e) => List(Checkbox(ctx, id, title(e), options(e)))
      case e: SocialSecurityNumber => List(Text(ctx, id, title(e))) // Should never happen in prod
      case _ => {
        logger.error("Could not convert element of type: " + element.getType)
        Nil
      }
    }
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

  def namedParentPath: List[String] = {
    parentsFromRootDown.flatMap {
      case t: Theme => List(t.getId)
      case p: Phase => List(p.getId)
      case t: TitledGroup => List(t.getId)
      case _ => Nil
    }
  }

  private def byType[T](xs: List[AnyRef])(implicit mf: Manifest[T]): List[T] = {
    xs.flatMap {
      case p: T => List(p)
      case _ => Nil
    }
  }
}