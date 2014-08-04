package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.elements.questions.{DropdownSelect, TextQuestion, CheckBox => HakuCheckBox, OptionQuestion => HakuOption, Radio => HakuRadio, TextArea => HakuTextArea}
import fi.vm.sade.haku.oppija.lomake.domain.elements.{Titled, HiddenValue, TitledGroup, Element, Notification => HakuNotification, Text => HakuText}
import fi.vm.sade.haku.oppija.lomake.validation.validators.RequiredFieldValidator
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.domain._
import scala.collection.JavaConversions._
import fi.vm.sade.omatsivut.domain.Notification

protected object FormQuestionFinder extends Logging {
  def findQuestionsByElementIds(contextElement: ElementWrapper, ids: Seq[String])(implicit lang: Language.Language): Set[QuestionLeafNode] = {
    val elements = ids.flatMap(contextElement.findById(_).toList).toSet
    findQuestionsFromElements(elements)
  }

  def findQuestionsFromElements(elementsToScan: Set[ElementWrapper])(implicit lang: Language.Language): Set[QuestionLeafNode] = {
    elementsToScan.flatMap { element =>
      element.getElementsOfType[Titled].flatMap { titled =>
        titledElementToQuestions(titled)
      }
    }
  }

  def findHiddenValues(contextElement: ElementWrapper): Set[(QuestionId, String)] = {
    contextElement.getElementsOfType[HiddenValue].map { hiddenValue =>
      val id = QuestionId(hiddenValue.phase.getId, hiddenValue.id)
      (id, hiddenValue.element.asInstanceOf[HiddenValue].getValue)
    }.toSet
  }

  private def titledElementToQuestions(elementWrapper: ElementWrapper)(implicit lang: Language.Language): List[QuestionLeafNode] = {
    val element = elementWrapper.element
    def id = QuestionId(elementWrapper.phase.getId, elementWrapper.id)
    def isRequired = element.getValidators.filter(o => o.isInstanceOf[RequiredFieldValidator]).nonEmpty

    element match {
      case e: TextQuestion =>
        List(Text(id, title(e), helpText(e), isRequired, maxLength(e)))
      case e: HakuTextArea =>
        val rows = toInt(element.getAttributes.toMap.getOrElse("rows", "3")).getOrElse(3)
        val cols = toInt(element.getAttributes.toMap.getOrElse("cols", "80")).getOrElse(80)
        List(TextArea(id, title(e), helpText(e), isRequired, maxLength(e), rows, cols))
      case e: HakuRadio =>
        List(Radio(id, title(e), helpText(e), dropDownOrRadioOptions(e), isRequired))
      case e: DropdownSelect =>
        List(Dropdown(id, title(e), helpText(e), dropDownOrRadioOptions(e), isRequired))
      case e: TitledGroup if containsCheckBoxes(e) =>
        val checkboxOptions = elementWrapper.getChildElementsOfType[HakuCheckBox].map(o => AnswerOption(title(o), o.id))
        List(Checkbox(id, title(e), helpText(e), checkboxOptions, isRequired))
      case e: HakuNotification => List(Notification(id, title(e), e.getNotificationType()))
      case e: HakuText => List(Label(id, title(e)))
      case _ => Nil
    }
  }

  private def containsCheckBoxes(e: TitledGroup): Boolean = {
    getImmediateChildElementsOfType[HakuCheckBox](e).nonEmpty
  }


  private def dropDownOrRadioOptions(e: HakuOption)(implicit lang: Language.Language): List[AnswerOption] = {
    e.getOptions.map(o => AnswerOption(title(o), o.getValue, o.isDefaultOption)).toList
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

  private def title(wrapper: ElementWrapper)(implicit lang: Language.Language): String = wrapper.element match {
    case e: Titled => title(e)
    case _ => wrapper.id
  }

  private def title[T <: Titled](e: T)(implicit lang: Language.Language): String = {
    val i18ntext = e.getI18nText
    if (i18ntext == null)
      ""
    else
      i18ntext.getTranslations.get(lang.toString())
  }
  private def helpText[T <: Titled](e: T)(implicit lang: Language.Language): String = {
    val help = e.getHelp()
    if (help == null)
      ""
    else
      help.getTranslations.get(lang.toString())
  }

  private def maxLength(element: Element) = {
    toInt(element.getAttributes.toMap.getOrElse("maxlength", "500")).getOrElse(500)
  }

  private def toInt(s: String):Option[Int] = { try { Some(s.toInt) } catch { case e:Exception => None } }

}