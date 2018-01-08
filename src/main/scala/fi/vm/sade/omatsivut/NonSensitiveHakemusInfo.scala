package fi.vm.sade.omatsivut

import fi.vm.sade.hakemuseditori.hakemus.HakemusInfo
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.Answers
import fi.vm.sade.hakemuseditori.hakemus.domain._
import fi.vm.sade.hakemuseditori.lomake.domain.{AnswerId, QuestionGroup, QuestionNode}
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants._
import org.json4s.JsonAST.JObject
import org.json4s.{CustomSerializer, Extraction}

class NonSensitiveHakemus(sensitiveHakemus: Hakemus, nonSensitiveAnswers: Set[AnswerId]) {
  val hakemus: Hakemus = sensitiveHakemus.copy(
    answers = NonSensitiveHakemusInfo.filterAnswers(sensitiveHakemus.answers, nonSensitiveAnswers ++ NonSensitiveHakemusInfo.nonSensitiveAnswers),
    state = sensitiveHakemus.state)
}

class NonSensitiveHakemusInfo(sensitiveHakemusInfo: HakemusInfo, nonSensitiveAnswers: Set[AnswerId]) {
  private val sensitiveAnswers = NonSensitiveHakemusInfo.answerIds(sensitiveHakemusInfo.hakemus.answers) &~ (nonSensitiveAnswers ++ NonSensitiveHakemusInfo.nonSensitiveAnswers)
  val hakemusInfo: HakemusInfo = sensitiveHakemusInfo.copy(
    hakemus = new NonSensitiveHakemus(sensitiveHakemusInfo.hakemus, nonSensitiveAnswers).hakemus,
    questions = removeQuestionsWithAnswers(sensitiveHakemusInfo.questions, sensitiveAnswers),
    tulosOk = true, // valintatulos is never shown, so always signal fetching it succeeded
    previewUrl = if (sensitiveHakemusInfo.hakemusSource == "Ataru") {
      sensitiveHakemusInfo.previewUrl
    } else {
      None
    }
  )

  def removeQuestionsWithAnswers(questions: List[QuestionNode], sensitiveAnswers: Set[AnswerId]): List[QuestionNode] = {
    questions.map({
      case qg: QuestionGroup => qg.copy(questions = removeQuestionsWithAnswers(qg.questions, sensitiveAnswers))
      case q: QuestionNode => q
    }).filterNot({
      case qg: QuestionGroup => qg.flatten.forall(qln => qln.answerIds.isEmpty)
      case q: QuestionNode => q.flatten.exists(_.answerIds.exists(sensitiveAnswers.contains))
    })
  }
}

class NonSensitiveHakemusSerializer extends CustomSerializer[NonSensitiveHakemus](formats => (
  {
    case o: JObject =>
      implicit val f = formats
      val hakemus = o.extract[Hakemus]
      new NonSensitiveHakemus(hakemus, NonSensitiveHakemusInfo.answerIds(hakemus.answers))
  },
  {
    case x: NonSensitiveHakemus =>
      implicit val f = formats
      Extraction.decompose(x.hakemus)
  }
  ))

class NonSensitiveHakemusInfoSerializer extends CustomSerializer[NonSensitiveHakemusInfo](formats => (
  {
    case o: JObject =>
      implicit val f = formats
      val hakemusInfo: HakemusInfo = o.extract[HakemusInfo]
      new NonSensitiveHakemusInfo(hakemusInfo, NonSensitiveHakemusInfo.answerIds(hakemusInfo.hakemus.answers))
  },
  {
    case x: NonSensitiveHakemusInfo =>
      implicit val f = formats
      Extraction.decompose(x.hakemusInfo)
  }
  ))

object NonSensitiveHakemusInfo {
  type Oid = String

  val nonSensitiveAnswers = Set(
    AnswerId(PHASE_PERSONAL, ELEMENT_ID_NICKNAME),
    AnswerId(PHASE_PERSONAL, ELEMENT_ID_LAST_NAME)
  )
  def sanitizeHakemusMuutos(hakemusMuutos: HakemusMuutos, nonSensitiveAnswers: Set[AnswerId]) = {
    hakemusMuutos.copy(answers = NonSensitiveHakemusInfo.filterAnswers(hakemusMuutos.answers, nonSensitiveAnswers ++ NonSensitiveHakemusInfo.nonSensitiveAnswers))
  }
  def answerIds(answers: Answers): Set[AnswerId] =
    answers.foldLeft(Set.empty[AnswerId]) {
      case (ids, (phaseId, phaseAnswers)) => ids ++ phaseAnswers.keys.map(AnswerId(phaseId, _))
    }
  def filterAnswers(answers: Answers, answerIds: Set[AnswerId]): Answers =
    answers.foldLeft(Map.empty[String, Map[String, String]]) {
      case (filteredAnswers, (phaseId, phaseAnswers)) =>
        val answers = phaseAnswers.filterKeys(questionId => answerIds.contains(AnswerId(phaseId, questionId)))
        filteredAnswers + (phaseId -> answers)
    }
}
