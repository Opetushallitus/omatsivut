package fi.vm.sade.hakemuseditori.lomake

import fi.vm.sade.hakemuseditori.hakemus.FlatAnswers.FlatAnswers
import fi.vm.sade.haku.oppija.lomake.domain.I18nText
import fi.vm.sade.haku.oppija.lomake.domain.elements.custom.GradeAverage
import fi.vm.sade.haku.oppija.lomake.domain.elements.questions.{DropdownSelect, TextQuestion, CheckBox => HakuCheckBox, OptionQuestion => HakuOption, Radio => HakuRadio, TextArea => HakuTextArea}
import fi.vm.sade.haku.oppija.lomake.domain.elements.{Notification => HakuNotification, Text => HakuText, _}
import fi.vm.sade.haku.oppija.lomake.validation.validators.RequiredFieldValidator
import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.lomake.domain._
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.hakulomakepohja.phase.lisatiedot.LisatiedotPhase
import fi.vm.sade.utils.slf4j.Logging
import scala.jdk.CollectionConverters._

object FormQuestionFinder extends Logging {

  def findQuestionsFromElements(elementsToScan: Set[ElementWrapper], answers: FlatAnswers)(implicit lang: Language.Language): Set[QuestionLeafNode] = {
    elementsToScan.flatMap { element =>
      element.getElementsOfType[Titled].flatMap { titled =>
        titledElementToQuestions(titled, answers)
      }
    }
  }

  def findHiddenValues(contextElement: ElementWrapper): Set[(QuestionId, String)] = {
    contextElement.getElementsOfType[HiddenValue].map { hiddenValue =>
      val id = QuestionId(hiddenValue.phase.map(_.getId).getOrElse(""), hiddenValue.id)
      (id, hiddenValue.element.asInstanceOf[HiddenValue].getValue)
    }.toSet
  }

  private def titledElementToQuestions(elementWrapper: ElementWrapper, answers: FlatAnswers)(implicit lang: Language.Language): List[QuestionLeafNode] = {
    val element = elementWrapper.element
    def id = QuestionId(elementWrapper.phase.map(_.getId).getOrElse(""), elementWrapper.id)
    def isRequired = element.getValidators.asScala.exists(_.isInstanceOf[RequiredFieldValidator])
    element match {
      case e: TextQuestion =>
        List(Text(id, title(e), helpText(e), verboseHelpText(e), isRequired, maxLength(e)))
      case e: HakuTextArea =>
        val rows = toInt(element.getAttributes.asScala.toMap.getOrElse("rows", "3")).getOrElse(3)
        val cols = toInt(element.getAttributes.asScala.toMap.getOrElse("cols", "80")).getOrElse(80)
        List(TextArea(id, title(e), helpText(e), verboseHelpText(e), isRequired, maxLength(e), rows, cols))
      case e: HakuRadio =>
        List(Radio(id, title(e), helpText(e), verboseHelpText(e), dropDownOrRadioOptions(e), isRequired))
      case e: DropdownSelect =>
        List(Dropdown(id, title(e), helpText(e), verboseHelpText(e), dropDownOrRadioOptions(e), isRequired))
      case e: TitledGroup =>
        val checkboxes = getImmediateChildElementsOfType[HakuCheckBox](element)
        if (checkboxes.isEmpty)
          List(Label(id, title(e)))
        else
          List(Checkbox(id, title(e), helpText(e), verboseHelpText(e),
            checkboxes.map(e => AnswerOption(title(e), e.getId)),
            isRequired))
      case e: HakuNotification => List(Notification(id, title(e), e.getNotificationType))
      case e: HakuText => List(Label(id, title(e)))
      case e: GradeAverage =>
        val checkboxes = getImmediateChildElementsOfType[HakuCheckBox](element)
        if (checkboxes.isEmpty)
          List(Label(id, gradeAverageTitle(e, answers)))
        else {
          List(GradeAverageCheckbox(QuestionId(id.phaseId, checkboxes.asJava.get(0).getId),
            gradeAverageTitle(e, answers), helpText(e), verboseHelpText(e), isRequired,
            checkboxes.map(e => AnswerOption(title(e), e.getId))))
        }
      case e: Theme if e.getId == LisatiedotPhase.OPPISOPIMUS_THEME_ID =>
        val checkboxes = getImmediateChildElementsOfType[HakuCheckBox](element)
        if (checkboxes.isEmpty)
          List(Label(id, title(e)))
        else
          List(Checkbox(id, title(e), helpText(e), verboseHelpText(e),
            checkboxes.map(e => AnswerOption(title(e), e.getId)),
            isRequired))
      case _ => Nil
    }
  }

  private def dropDownOrRadioOptions(e: HakuOption)(implicit lang: Language.Language): List[AnswerOption] = {
    e.getOptions.asScala.map(o => AnswerOption(title(o), o.getValue, o.isDefaultOption)).toList
  }


  private def getImmediateChildElementsOfType[A](rootElement: Element)(implicit mf : Manifest[A]): List[A] = {
    rootElement.getChildren.asScala.toList.flatMap { child =>
      if (mf.runtimeClass.isAssignableFrom(child.getClass)) {
        List(child.asInstanceOf[A])
      } else {
        Nil
      }
    }
  }

  private def gradeAverageTitle(e: GradeAverage, answers: FlatAnswers)(implicit lang: Language.Language): String = {
    val nimike = answers.getOrElse(e.getRelatedNimikeId, "nimikeFallback") match {
      case "399999" => answers(e.getRelatedMuuNimike)
      case koodi => textToTranslatedString(() => e.getAmmattitutkintonimikkeet().asScala(koodi).getI18nText)
    }
    val oppilaitos = answers.get(e.getRelatedOppilaitosId).map {
      case "1.2.246.562.10.57118763579" => " (" + answers(e.getRelatedMuuOppilaitos) + ")"
      case koodi => " (" + textToTranslatedString(() => e.getOppilaitokset().asScala(koodi.replace(".", "_")).getI18nText) + ")"
    }.getOrElse("")
    nimike + oppilaitos
  }

  private def title[T <: Titled](e: T)(implicit lang: Language.Language): String = {
    textToTranslatedString({() => e.getI18nText })
  }

  private def helpText[T <: Titled](e: T)(implicit lang: Language.Language): String = {
    textToTranslatedString({() => e.getHelp})
  }

  private def verboseHelpText[T <: Titled](e: T)(implicit lang: Language.Language): String = {
    textToTranslatedString({() => e.getVerboseHelp })
  }

  private def textToTranslatedString[T <: Titled](f: () => I18nText)(implicit lang: Language.Language): String = {
    val text = f()
    if(text == null)
      ""
    else
      text.getText(lang.toString())
  }

  private def maxLength(element: Element) = {
    toInt(element.getAttributes.asScala.toMap.getOrElse("maxlength", "500")).getOrElse(500)
  }

  private def toInt(s: String):Option[Int] = { try { Some(s.toInt) } catch { case e:Exception => None } }

}
