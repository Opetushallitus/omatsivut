package fi.vm.sade.omatsivut.lomake

import fi.vm.sade.haku.oppija.lomake.domain.elements.{Element, Titled}
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.lomake.domain.{QuestionGroup, QuestionLeafNode}

object QuestionGrouper {
  def groupQuestionsByStructure(contextElement: ElementWrapper, foundQuestions: Set[(QuestionLeafNode)])(implicit lang: Language.Language): List[QuestionGroup] = {
    foundQuestions
      .map { question => (question, contextElement.findById(question.id.questionId)) }
      .filter(_._2.isDefined)
      .map(tuple => (tuple._1, tuple._2.get))
      .groupBy { case (question, elementContext) => elementContext.namedParents}
      .toList
      .sortBy { case (path, questions) => path.asInstanceOf[List[Element]]}(ParentPathOrdering)
      .map {
      case (parents, questions) =>
      val groupNamePath = parents.tail
          .filter { t: Titled => t.getI18nText != null}
          .map(_.getI18nText.getTranslations.get(lang.toString()))
        val groupName = groupNamePath.mkString("", " - ", "")

        val sortedQuestions = questions.toList
          .sortBy { case (question, elementContext) => elementContext.selfAndParents}(ParentPathOrdering)
          .map { case (question, elementContext) => question}

        QuestionGroup(groupName, sortedQuestions)
    }
  }
}

private object ParentPathOrdering extends scala.math.Ordering[List[Element]] {
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
