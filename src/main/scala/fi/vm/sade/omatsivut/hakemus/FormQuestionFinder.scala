package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.elements.custom.SocialSecurityNumber
import fi.vm.sade.haku.oppija.lomake.domain.elements.questions.{DropdownSelect, TextQuestion, CheckBox => HakuCheckBox, OptionQuestion => HakuOption, Radio => HakuRadio, TextArea => HakuTextArea}
import fi.vm.sade.haku.oppija.lomake.domain.elements._
import fi.vm.sade.haku.oppija.lomake.validation.validators.RequiredFieldValidator
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.domain.Text
import fi.vm.sade.omatsivut.domain._
import scala.collection.JavaConversions._
import fi.vm.sade.omatsivut.domain.Notification

protected object FormQuestionFinder extends Logging {
  def findQuestions(contextElement: Element, elementsToScan: Set[Element]): List[QuestionGroup] = {
    elementsToScan.flatMap { element =>
        getElementsOfType[Titled](element).flatMap { titled =>
          titledElementToQuestions(contextElement, titled)
        }
      }
      .groupBy { case (question, elementContext) => elementContext.namedParents}
      .toList
      .sortBy{case (path, questions) => path.asInstanceOf[List[Element]]}(ParentPathOrdering())
      .map {
        case (parents, questions) =>
          val lang = "fi" // TODO: kieliversiot
          val groupNamePath = parents.tail.map(_.getI18nText.getTranslations.get(lang))
          val groupName = groupNamePath.mkString("", " - ", "")

          val sortedQuestions = questions.toList
            .sortBy { case (question, elementContext) => elementContext.selfAndParents }(ParentPathOrdering())
            .map {case (question, elementContext) => question }

          QuestionGroup(groupName, sortedQuestions)
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

  private def options(e: HakuOption): List[AnswerOption] = {
    e.getOptions.map(o => AnswerOption(title(o), o.getValue, o.isDefaultOption)).toList
  }
  private def options(e: TitledGroup): List[AnswerOption] = {
    getChildElementsOfType[HakuCheckBox](e).map(o => AnswerOption(title(o), o.getId()))
  }
  private def title[T <: Titled](e: T): String = {
    e.getI18nText.getTranslations.get("fi") // TODO: kieliversiot
  }
  private def helpText[T <: Titled](e: T): String = {
    val help = e.getHelp()
    if (help == null)
      ""
    else
      help.getTranslations.get("fi") // TODO: kieliversiot
  }

  private def titledElementToQuestions(contextElement: Element, element: Titled): List[(QuestionNode, ElementContext)] = {
    val elementContext = new ElementContext(contextElement, element)
    def id = QuestionId(elementContext.phase.getId, element.getId)
    def isRequired = element.getValidators.filter(o => o.isInstanceOf[RequiredFieldValidator]).nonEmpty
    def maxlength = toInt(element.getAttributes.toMap.getOrElse("maxlength", "500")).getOrElse(500)
    def rows = toInt(element.getAttributes.toMap.getOrElse("rows", "3")).getOrElse(3)
    def cols = toInt(element.getAttributes.toMap.getOrElse("cols", "80")).getOrElse(80)
    def toInt(s: String):Option[Int] = { try { Some(s.toInt) } catch { case e:Exception => None } }
    def containsCheckBoxes(e: TitledGroup): Boolean = {
      getImmediateChildElementsOfType[HakuCheckBox](e).nonEmpty
    }

    (element match {
      case e: TextQuestion => List(Text(id, title(e), helpText(e), isRequired, maxlength))
      case e: HakuTextArea => List(TextArea(id, title(e), helpText(e), isRequired, maxlength, rows, cols))
      case e: HakuRadio => List(Radio(id, title(e), helpText(e), options(e), isRequired))
      case e: DropdownSelect => List(Dropdown(id, title(e), helpText(e), options(e), isRequired))
      case e: TitledGroup if containsCheckBoxes(e) => List(Checkbox(id, title(e), helpText(e), options(e), isRequired))
      case e: TitledGroup => Nil
      case e: HakuCheckBox => Nil
      case e: fi.vm.sade.haku.oppija.lomake.domain.elements.Notification => List(Notification(title(e), e.getNotificationType()))
      case e: fi.vm.sade.haku.oppija.lomake.domain.elements.Text => List(Label(title(e)))
      case _ => {
        logger.error("Could not convert element of type: " + element.getType + " with title: " + title(element))
        Nil
      }
    }).map { question => (question, elementContext)}
  }
}

private case class ParentPathOrdering() extends scala.math.Ordering[List[Element]] {
  override def compare(left: List[Element], right: List[Element]) = {
    val leftOrderingPath: List[Int] = orderingPath(left)
    val rightOrderingPath: List[Int] = orderingPath(right)
    compareOrderingPath(leftOrderingPath, rightOrderingPath)
  }

  private def orderingPath(parentPath: List[Element]): List[Int] = {
    parentPath.zip(parentPath.tail).map {
      case (mother, child) =>
        mother.getChildren.indexOf(child)
    }
  }

  private def compareOrderingPath(p1: List[Int], p2: List[Int]): Int = (p1, p2) match {
    case (x :: xs, y :: ys) =>
      if (x < y) {
        -1
      } else if (x > y) {
        1
      } else {
        compareOrderingPath(xs, ys)
      }
    case (x :: xs, Nil) => 1
    case (Nil, y :: ys) => -1
    case _ => 0
  }
}

class ElementContext(val contextElement: Element, val element: Titled) {
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

  private def byType[T](xs: List[AnyRef])(implicit mf: Manifest[T]): List[T] = {
    xs.flatMap {
      case p: T => List(p)
      case _ => Nil
    }
  }
}