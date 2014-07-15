package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.domain.elements.custom.SocialSecurityNumber
import fi.vm.sade.haku.oppija.lomake.domain.elements.{Element, Phase, Titled}
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.domain._
import fi.vm.sade.haku.oppija.lomake.domain.elements.questions.{DropdownSelect, TextQuestion, OptionQuestion => HakuOption, Radio => HakuRadio, TextArea => HakuTextArea}
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

  private def getPhases(applicationSystem: ApplicationSystem) = {
    getElementsOfType[Phase](applicationSystem.getForm)
  }

  private def getElementsOfType[A](rootElement: Element)(implicit mf : Manifest[A]): List[A] = {
    def convertChildElements(element: Element): List[A] = {
      element.getChildren.toList.flatMap { child => getForChildElements(child)}
    }
    def getForChildElements(element: Element): List[A] = {
      if (mf.runtimeClass.isAssignableFrom(element.getClass)) {
        element.asInstanceOf[A] :: convertChildElements(element)
      } else {
        convertChildElements(element)
      }
    }
    convertChildElements(rootElement)
  }


  private def titledElementToQuestions(phase: Phase, element: Titled): List[Question] = {
    def id = QuestionId(phase.getId, element.getId)
    def title(e: Titled): Translations = {
      Translations(e.getI18nText.getTranslations.toMap)
    }
    def options(e: HakuOption): List[Choice] = {
      e.getOptions.map(o => Choice(title(o), o.isDefaultOption)).toList
    }

    element match {
      case e: TextQuestion => List(Text(id, title(e)))
      case e: HakuTextArea => List(TextArea(id, title(e)))
      case e: HakuRadio => List(Radio(id, title(e), options(e)))
      case e: DropdownSelect => List(Dropdown(id, title(e), options(e)))
      case e: SocialSecurityNumber => List(Text(id, title(e))) // Should never happen in prod
      case _ => {
        logger.error("Could not convert element of type: " + element.getType)
        Nil
      }
    }
  }
}
