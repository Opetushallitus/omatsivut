package fi.vm.sade.omatsivut

import fi.vm.sade.hakemuseditori.hakemus.HakemusInfo
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.Answers
import fi.vm.sade.hakemuseditori.lomake.domain.{AnswerId, QuestionNode}
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants._

object NonSensitiveHakemusInfo {
  type Oid = String

  val nonSensitiveContactDetails = List(ELEMENT_ID_EMAIL, ELEMENT_ID_PREFIX_PHONENUMBER + "1")

  protected case class NonSensitiveHakemusInfo(hakemusInfo: HakemusInfo, jsonWebToken: String)

  def extend[A, B, C](target: Map[A, Map[B, C]], source: Map[A, Map[B, C]]): Map[A, Map[B, C]] =
    target ++ (for ((k, v) <- source) yield k -> (target.getOrElse(k, Map()) ++ v))

  def matchingAnswer(answers: Answers, id: AnswerId): Answers =
    answers.get(id.phaseId).flatMap(_.get(id.questionId))
      .map(a => Map(id.phaseId -> Map(id.questionId -> a)))
      .getOrElse(Map())

  def answersWithQuestions(originalAnswers: Answers, questions: List[QuestionNode]): Answers = {
    val answerIds: List[AnswerId] = questions.flatMap(q => q.flatten.flatMap(l => l.answerIds))
    answerIds.map(matchingAnswer(originalAnswers, _)).fold(Map())(extend)
  }

  def apply(sensitiveHakemusInfo: HakemusInfo, jsonWebToken: String): NonSensitiveHakemusInfo = {
    val nonSensitiveHenkilotiedot = sensitiveHakemusInfo.hakemus.answers.get(PHASE_PERSONAL) match {
      case Some(henkilotiedot) =>
        nonSensitiveContactDetails
          .map(key => key -> henkilotiedot.getOrElse(key, ""))
          .toMap
      case None => Map.empty[String, String]
    }
    val nonSensitiveQuestions = sensitiveHakemusInfo.questions.filter(q => !q.isSensitive)
    NonSensitiveHakemusInfo(
      sensitiveHakemusInfo.copy(
        hakemus = sensitiveHakemusInfo.hakemus.copy(
          answers = extend(answersWithQuestions(sensitiveHakemusInfo.hakemus.answers, nonSensitiveQuestions),
            Map(PHASE_PERSONAL -> nonSensitiveHenkilotiedot))
        ),
        questions = nonSensitiveQuestions
      ), jsonWebToken)
  }
}