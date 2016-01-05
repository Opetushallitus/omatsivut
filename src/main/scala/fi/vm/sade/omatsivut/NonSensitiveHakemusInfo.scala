package fi.vm.sade.omatsivut

import fi.vm.sade.hakemuseditori.hakemus.HakemusInfo
import fi.vm.sade.hakemuseditori.hakemus.domain._
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.Answers
import fi.vm.sade.hakemuseditori.lomake.domain.{AnswerId, QuestionNode}
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants._

object NonSensitiveHakemusInfo {
  type Oid = String

  val nonSensitiveContactDetails = List(ELEMENT_ID_EMAIL, ELEMENT_ID_PREFIX_PHONENUMBER + "1", ELEMENT_ID_NICKNAME, ELEMENT_ID_LAST_NAME)

  protected case class NonSensitiveHakemusInfo(hakemusInfo: HakemusInfo)

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

  def nonSensitiveHenkilotiedot(hakemus: Hakemus): Answers =
    Map(PHASE_PERSONAL -> (hakemus.answers.get(PHASE_PERSONAL) match {
      case Some(henkilotiedot) =>
        nonSensitiveContactDetails
          .map(key => key -> henkilotiedot.getOrElse(key, ""))
          .toMap
      case None => Map.empty[String, String]
    }))

  def sanitizeHakemus(hakemus: Hakemus, visibleQuestions: List[QuestionNode]): Hakemus = {
    val filteredAnswers = answersWithQuestions(hakemus.answers, visibleQuestions)
    val stateWithoutValintatulos = hakemus.state match {
      case s: Active => s.copy(valintatulos = None)
      case s: HakukausiPaattynyt => s.copy(valintatulos = None)
      case s: HakukierrosPaattynyt => s.copy(valintatulos = None)
      case s: HakemuksenTila => s
    }
    hakemus.copy(
      answers = extend(filteredAnswers, nonSensitiveHenkilotiedot(hakemus)),
      state = stateWithoutValintatulos)
  }

  def apply(sensitiveHakemusInfo: HakemusInfo, questions: List[QuestionNode]): NonSensitiveHakemusInfo = {
    val nonSensitiveQuestions = questions // TODO
    NonSensitiveHakemusInfo(
      sensitiveHakemusInfo.copy(
        hakemus = sanitizeHakemus(sensitiveHakemusInfo.hakemus, nonSensitiveQuestions),
        questions = nonSensitiveQuestions
      ))
  }
}
