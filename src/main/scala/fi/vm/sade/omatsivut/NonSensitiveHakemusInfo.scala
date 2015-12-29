package fi.vm.sade.omatsivut

import fi.vm.sade.hakemuseditori.hakemus.HakemusInfo
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.Answers
import fi.vm.sade.hakemuseditori.lomake.domain.{AnswerId, QuestionLeafNode, QuestionNode}
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants._

object NonSensitiveHakemusInfo {
  type Oid = String

  protected case class NonSensitiveHakemusInfo(hakemusInfo: HakemusInfo, jsonWebToken: String)

  def extend[A, B, C](target: Map[A, Map[B, C]], source: Map[A, Map[B, C]]): Map[A, Map[B, C]] =
    target ++ (for ((k, v) <- source) yield k -> (v ++ target.getOrElse(k, Map())))

  def matchingAnswer(answers: Answers, id: AnswerId): Answers =
    answers.get(id.phaseId).flatMap(_.get(id.questionId))
      .map(a => Map(id.phaseId -> Map(id.questionId -> a)))
      .getOrElse(Map())

  def matchingAnswer(originalAnswers: Answers, questionLeafNode: QuestionLeafNode): Answers =
    questionLeafNode.answerIds
      .map(matchingAnswer(originalAnswers, _))
      .foldLeft(Map.empty[String, Map[String, String]])(extend)

  def filteredAnswers(originalAnswers: Answers, questions: List[QuestionNode]): Answers = {
    questions.foldLeft(Map.empty[String, Map[String, String]])((qs, nodes) =>
      nodes.flatten
        .map(matchingAnswer(originalAnswers, _))
        .foldLeft(Map.empty[String, Map[String, String]])(extend))
  }

  def apply(sensitiveHakemusInfo: HakemusInfo, jsonWebToken: String): NonSensitiveHakemusInfo = {
    sensitiveHakemusInfo.hakemus.answers.get(PHASE_PERSONAL) match {
      case Some(henkilotiedot) =>
        val nonSensitiveContactDetails = List(ELEMENT_ID_EMAIL, ELEMENT_ID_PREFIX_PHONENUMBER + "1")
          .map(key => key -> henkilotiedot.getOrElse(key, ""))
          .toMap
        NonSensitiveHakemusInfo(
          sensitiveHakemusInfo.copy(
            hakemus = sensitiveHakemusInfo.hakemus.copy(
              answers = Map(PHASE_PERSONAL -> nonSensitiveContactDetails)
            ),
            questions = sensitiveHakemusInfo.questions.filter(q => !q.isSensitive)
          ), jsonWebToken)

      case _ => throw new RuntimeException("henkilotiedot missing for hakemus " + sensitiveHakemusInfo.hakemus.oid)
    }
  }
}