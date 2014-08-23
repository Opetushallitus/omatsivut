package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.elements.{Titled, Element}
import fi.vm.sade.omatsivut.domain.{QuestionNode, QuestionGroup, QuestionLeafNode, Language}

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

  def avoidDoubles(contextElement: ElementWrapper, questions: List[QuestionNode])(implicit lang: Language.Language) : List[QuestionNode] = {
    def getDuplicates = {
      val n:List[QuestionLeafNode] = questions.flatMap(_.flatten)
      val groups = n.groupBy { node => node.id }
      groups.filter { case (_, nodes) => nodes.length > 1 }.map(_._2.head)
    }

    val duplicates = getDuplicates
    val duplicateIds = duplicates.map(_.id).toSet

    val withoutDuplicates = questions.map { item => item match {
        case group: QuestionGroup =>
          group.filter { (question) => !duplicateIds.contains(question.id) }
        case _ =>
          item
      }
    }

    withoutDuplicates ::: QuestionGrouper.groupQuestionsByStructure(contextElement, duplicates.toSet)
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
