package fi.vm.sade.omatsivut

import fi.vm.sade.hakemuseditori.lomake.domain.{QuestionLeafNode, AnswerId, QuestionId}
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import fi.vm.sade.omatsivut.hakemus.{FixturePerson, HakemusApiSpecification}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class NonSensitiveHakemusInfoSpec extends HakemusApiSpecification with FixturePerson with TimeWarp {

  "NonSensitiveHakemusInfo" should {

    // TODO: fix me
    /*
    "return only answers that have questions" in {
      withHakemus(hakemusKorkeakoulutKevat2014Id) { hakemusInfo =>

        case class NonSesitiveQuestion(title: String, id: QuestionId, answerIds: List[AnswerId]) extends QuestionLeafNode()

        val modifiedHakemusInfo = hakemusInfo.copy(questions = hakemusInfo.questions ++ List(
          NonSesitiveQuestion("nonsensitive question", QuestionId("lorem", "questionId"), List(AnswerId("koulutustausta", "pohjakoulutus_yo_vuosi")))
        ))
        val nonSensitiveInfo = NonSensitiveHakemusInfo.apply(modifiedHakemusInfo, List())
        nonSensitiveInfo.hakemusInfo.hakemus.answers.size must_== 2
        nonSensitiveInfo.hakemusInfo.hakemus.answers.get("koulutustausta").get.size must_== 1
        nonSensitiveInfo.hakemusInfo.hakemus.answers.get("koulutustausta").get.get("pohjakoulutus_yo_vuosi") must_== Some("2014")
      }
    }*/

    "always has nonsensitive contact information" in {
      withHakemus(hakemusKorkeakoulutKevat2014Id) { hakemusInfo =>
        val nonSensitiveInfo = NonSensitiveHakemusInfo.sanitize(hakemusInfo, NonSensitiveHakemusInfo.nonSensitiveAnswers)
        nonSensitiveInfo.hakemusInfo.hakemus.answers.size must_== 1
        nonSensitiveInfo.hakemusInfo.hakemus.answers.get("henkilotiedot").get.size must_== NonSensitiveHakemusInfo.nonSensitiveContactDetails.size

        val foundKeys = NonSensitiveHakemusInfo.nonSensitiveContactDetails.map(key =>
          nonSensitiveInfo.hakemusInfo.hakemus.answers.get("henkilotiedot").get.get(key)
        )

        foundKeys.count(_.isDefined) must_== NonSensitiveHakemusInfo.nonSensitiveContactDetails.size
      }
    }

  }

}
