package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.domain.elements.custom.SocialSecurityNumber
import fi.vm.sade.haku.oppija.lomake.domain.elements.{TitledGroup, Element, Phase, Titled}
import fi.vm.sade.omatsivut.Logging
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
  def findQuestions(phase: Phase, element: Element): List[Question] = {
    getElementsOfType[Titled](element).flatMap { titled =>
      titledElementToQuestions(phase, titled)
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

  private def titledElementToQuestions(phase: Phase, element: Titled): List[Question] = {
    def id = QuestionId(phase.getId, element.getId)
    def containsCheckBoxes(e: TitledGroup): Boolean = {
      getChildElementsOfType[HakuCheckBox](e).nonEmpty
    }

    element match {
      case e: TextQuestion => List(Text(id, title(e)))
      case e: HakuTextArea => List(TextArea(id, title(e)))
      case e: HakuRadio => List(Radio(id, title(e), options(e)))
      case e: DropdownSelect => List(Dropdown(id, title(e), options(e)))
      case e: TitledGroup if containsCheckBoxes(e) => List(Checkbox(id, title(e), options(e)))
      case e: SocialSecurityNumber => List(Text(id, title(e))) // Should never happen in prod
      case _ => {
        logger.error("Could not convert element of type: " + element.getType)
        Nil
      }
    }
  }
}
