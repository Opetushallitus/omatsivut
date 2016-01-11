package fi.vm.sade.omatsivut

import fi.vm.sade.hakemuseditori.hakemus.HakemusInfo
import fi.vm.sade.hakemuseditori.hakemus.domain._
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.Answers
import fi.vm.sade.hakemuseditori.lomake.domain.{QuestionGroup, AnswerId, QuestionNode}
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants._

object NonSensitiveHakemusInfo {
  type Oid = String

  val nonSensitiveContactDetails = List(ELEMENT_ID_EMAIL, ELEMENT_ID_PREFIX_PHONENUMBER + "1", ELEMENT_ID_NICKNAME, ELEMENT_ID_LAST_NAME)
  val nonSensitiveAnswers = Set(
    AnswerId(PHASE_PERSONAL, ELEMENT_ID_EMAIL),
    AnswerId(PHASE_PERSONAL, ELEMENT_ID_PREFIX_PHONENUMBER + "1"),
    AnswerId(PHASE_PERSONAL, ELEMENT_ID_NICKNAME),
    AnswerId(PHASE_PERSONAL, ELEMENT_ID_LAST_NAME)
  )

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

  def answerIds(answers: Answers): Set[AnswerId] =
    answers.foldLeft(Set.empty[AnswerId]) {
      case (ids, (phaseId, phaseAnswers)) => ids ++ phaseAnswers.keys.map(AnswerId(phaseId, _))
    }

  def removeQuestionsWithAnswers(questions: List[QuestionNode], sensitiveAnswers: Set[AnswerId]): List[QuestionNode] = {
    questions.map({
      case qg: QuestionGroup => qg.copy(questions = removeQuestionsWithAnswers(qg.questions, sensitiveAnswers))
      case q: QuestionNode => q
    }).filterNot({
      case qg: QuestionGroup => qg.flatten.forall(qln => qln.answerIds.isEmpty)
      case q: QuestionNode => q.flatten.exists(_.answerIds.exists(sensitiveAnswers.contains))
    })
  }

  def filterAnswers(answers: Answers, answerIds: Set[AnswerId]): Answers =
    answers.foldLeft(Map.empty[String, Map[String, String]]) {
      case (filteredAnswers, (phaseId, phaseAnswers)) =>
        val answers = phaseAnswers.filterKeys(questionId => answerIds.contains(AnswerId(phaseId, questionId)))
        if (answers.isEmpty) {
          filteredAnswers
        } else {
          filteredAnswers + (phaseId -> answers)
        }
    }

  def sanitizeHakemus(hakemus: Hakemus, nonSensitiveAnswers: Set[AnswerId]): Hakemus =
    hakemus.copy(
      answers = filterAnswers(hakemus.answers, nonSensitiveAnswers),
      state = hakemus.state match {
      case s: Active => s.copy(valintatulos = None)
      case s: HakukausiPaattynyt => s.copy(valintatulos = None)
      case s: HakukierrosPaattynyt => s.copy(valintatulos = None)
      case s: HakemuksenTila => s
    })

  def sanitize(hakemusInfo: HakemusInfo, nonSensitiveAnswers: Set[AnswerId]): NonSensitiveHakemusInfo = {
    val sensitiveAnswers = answerIds(hakemusInfo.hakemus.answers) &~ nonSensitiveAnswers
    NonSensitiveHakemusInfo(
      hakemusInfo.copy(
        hakemus = sanitizeHakemus(hakemusInfo.hakemus, nonSensitiveAnswers),
        questions = removeQuestionsWithAnswers(hakemusInfo.questions, sensitiveAnswers)
      )
    )
  }
}
