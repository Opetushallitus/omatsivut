package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.elements.custom.SocialSecurityNumber
import fi.vm.sade.haku.oppija.lomake.domain.elements.questions.{DropdownSelect, TextQuestion, CheckBox => HakuCheckBox, OptionQuestion => HakuOption, Radio => HakuRadio, TextArea => HakuTextArea}
import fi.vm.sade.haku.oppija.lomake.domain.elements._
import fi.vm.sade.haku.oppija.lomake.util.ElementTree
import fi.vm.sade.haku.oppija.lomake.validation.validators.RequiredFieldValidator
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.domain.Hakemus.Answers
import fi.vm.sade.omatsivut.domain.Text
import fi.vm.sade.omatsivut.domain._
import scala.collection.JavaConversions._
import fi.vm.sade.omatsivut.domain.Notification

protected object FormQuestionFinder extends Logging {
  def findQuestionsByElementIds(contextElement: ElementWrapper, ids: Seq[String]): Set[QuestionLeafNode] = {
    val elements = ids.flatMap(contextElement.findById(_).toList).toSet
    findQuestionsFromElements(elements)
  }

  def findQuestionsFromElements(elementsToScan: Set[ElementWrapper]): Set[QuestionLeafNode] = {
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

  private def titledElementToQuestions(elementWrapper: ElementWrapper): List[QuestionLeafNode] = {
    val element = elementWrapper.element
    def id = QuestionId(elementWrapper.phase.getId, elementWrapper.id)
    def isRequired = element.getValidators.filter(o => o.isInstanceOf[RequiredFieldValidator]).nonEmpty
    def maxlength = toInt(element.getAttributes.toMap.getOrElse("maxlength", "500")).getOrElse(500)
    def rows = toInt(element.getAttributes.toMap.getOrElse("rows", "3")).getOrElse(3)
    def cols = toInt(element.getAttributes.toMap.getOrElse("cols", "80")).getOrElse(80)
    def toInt(s: String):Option[Int] = { try { Some(s.toInt) } catch { case e:Exception => None } }
    def containsCheckBoxes(e: TitledGroup): Boolean = {
      getImmediateChildElementsOfType[HakuCheckBox](e).nonEmpty
    }

    element match {
      case e: TextQuestion => List(Text(id, title(e), helpText(e), isRequired, maxlength))
      case e: HakuTextArea => List(TextArea(id, title(e), helpText(e), isRequired, maxlength, rows, cols))
      case e: HakuRadio => List(Radio(id, title(e), helpText(e), options(e), isRequired))
      case e: DropdownSelect => List(Dropdown(id, title(e), helpText(e), options(e), isRequired))
      case e: TitledGroup if containsCheckBoxes(e) => List(Checkbox(id, title(e), helpText(e), options(elementWrapper), isRequired))
      case e: TitledGroup => Nil
      case e: HakuCheckBox => Nil
      case e: fi.vm.sade.haku.oppija.lomake.domain.elements.Notification => List(Notification(id, title(e), e.getNotificationType()))
      case e: fi.vm.sade.haku.oppija.lomake.domain.elements.Text => List(Label(id, title(e)))
      case _ => Nil
    }
  }

  private def options(e: HakuOption): List[AnswerOption] = {
    e.getOptions.map(o => AnswerOption(title(o), o.getValue, o.isDefaultOption)).toList
  }
  private def options(e: ElementWrapper): List[AnswerOption] = {
    e.getChildElementsOfType[HakuCheckBox].map(o => AnswerOption(title(o), o.id))
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

  private def title(wrapper: ElementWrapper): String = wrapper.element match {
    case e: Titled => title(e)
    case _ => wrapper.id
  }

  private def title[T <: Titled](e: T): String = {
    val i18ntext = e.getI18nText
    if (i18ntext == null)
      ""
    else
      i18ntext.getTranslations.get("fi") // TODO: kieliversiot
  }
  private def helpText[T <: Titled](e: T): String = {
    val help = e.getHelp()
    if (help == null)
      ""
    else
      help.getTranslations.get("fi") // TODO: kieliversiot
  }
}