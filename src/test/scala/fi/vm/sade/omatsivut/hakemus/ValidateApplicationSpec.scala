package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.Answers
import fi.vm.sade.hakemuseditori.hakemus.domain._
import fi.vm.sade.hakemuseditori.lomake.domain.{QuestionId, QuestionNode}
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.omatsivut.PersonOid
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import org.json4s._
import org.json4s.jackson.{JsonMethods, Serialization}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.specification.BeforeEach

@RunWith(classOf[JUnitRunner])
class ValidateApplicationSpec extends HakemusApiSpecification with FixturePerson with BeforeEach {
  override def before: Unit = {
    fixtureImporter.applyFixtures()
  }

  private val hakemusNivelKesa2013WithPeruskouluBaseEducationExtraQuestions = List(
    "Miksi haet kymppiluokalle?",
    "Haen ensisijaisesti kielitukikympille?",
    "Turun Kristillinen opisto",
    "Päättötodistuksen kaikkien oppiaineiden keskiarvo?",
    "Päättötodistukseni on"
  )

  sequential

  "POST /application/validate" should {
    "validating unchanged application should return same questions and errors as initial load" in {
      withHakemusWithEmptyAnswers(hakemusNivelKesa2013WithPeruskouluBaseEducationId) { hakemusInfo =>
        validate(hakemusInfo.hakemus) { (errors, structuredQuestions) =>
          status must_== 200
          errors must_== List()
          errors must_== hakemusInfo.errors
          QuestionNode.flatten(structuredQuestions).map(_.title) must_== hakemusNivelKesa2013WithPeruskouluBaseEducationExtraQuestions
          structuredQuestions must_== hakemusInfo.questions
        }
      }
    }

    "reject application with different personOid" in {
      withHakemusWithEmptyAnswers(hakemusNivelKesa2013WithPeruskouluBaseEducationId) { hakemusInfo =>
        validate(hakemusInfo.hakemus) { (errors, structuredQuestions) =>
          status must_== 500
        }(PersonOid("wat"))
      }
    }

    "validate application with extra answers" in {
      val extraQuestionOne: (Hakemus) => Hakemus = answerExtraQuestion(osaaminen, "osaaminen-tuntematon-kysymys", "osaaminen-testivastaus")
      val extraQuestionTwo: (Hakemus) => Hakemus = answerExtraQuestion(hakutoiveet, "hakutoive-tuntematon-kysymys", "osaaminen-testivastaus")
      modifyHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId)(extraQuestionOne andThen extraQuestionTwo) { newHakemus =>
        validate(newHakemus) { (errors, structuredQuestions) =>
          status must_== 200
          errors must_== List()
          QuestionNode.flatten(structuredQuestions).map(_.title) must_== hakemusNivelKesa2013WithPeruskouluBaseEducationExtraQuestions
        }
      }
    }

    "get additional question correctly for old questions" in {
      withHakemusWithEmptyAnswers(TestFixture.hakemusWithGradeGridAndDancePreference) { hakemusInfo =>
        validate(hakemusInfo.hakemus) { (errors, structuredQuestions) =>
          status must_== 200
          QuestionNode.flatten(structuredQuestions).map(_.id) must_== List(
             QuestionId("hakutoiveet","preference1-discretionary"),
             QuestionId("hakutoiveet","preference1_kaksoistutkinnon_lisakysymys"),
             QuestionId("hakutoiveet","preference2-discretionary"),
             QuestionId("hakutoiveet","preference2_urheilijan_ammatillisen_koulutuksen_lisakysymys"),
             QuestionId("hakutoiveet","preference2_kaksoistutkinnon_lisakysymys"),
             QuestionId("hakutoiveet","preference3-discretionary"),
             QuestionId("lisatiedot","TYOKOKEMUSKUUKAUDET")
          )
        }
      }
    }

    "validate duplicate entries in different applications for user" in {
      val dao = springContext.applicationDAO
      val applicationOid1 = "1.2.246.562.11.00000441371"
      val applicationOid2 = "1.2.246.562.11.00000441374"
      val application1 = dao.find(new Application().setOid(applicationOid1)).get(0)
      val application2 = application1.clone().setOid(applicationOid2)
      dao.save(replaceAnswers(application2, duplicateAnswers(1)))

      withHakemus(TestFixture.hakemusLisahaku) { hakemusInfo =>
        val working = replaceApplicationWishes(hakemusInfo.hakemus.answers, workingAnswers(2))
        val duplicate = replaceApplicationWishes(hakemusInfo.hakemus.answers, duplicateAnswers(2))
        val validationError = ValidationError("preference2-Koulutus", "Et voi syöttää samaa hakutoivetta useaan kertaan. Hakutoive löytyy jo toisesta hakemuksesta")

        validate(hakemusInfo.hakemus.copy(answers = duplicate)) { (errors, questions) =>
          status must_== 200
          errors must_== List(validationError)
        }

        validate(hakemusInfo.hakemus.copy(answers = working)) { (errors, questions) =>
          status must_== 200
          errors must_== Nil
        }

        // For the next 2 we are only interested if our error code for duplicate entries is in the result list
        validatePut(hakemusInfo.hakemus.copy(answers = duplicate), "secure/applications/")(_.extract[List[ValidationError]]) { errors =>
          status must_== 400
          errors must contain(validationError)
        }

        validatePut(hakemusInfo.hakemus.copy(answers = working), "secure/applications/")(_.extract[List[ValidationError]]) { errors =>
          errors must not(contain(validationError))
        }
      }
    }
  }

  def replaceAnswers(application: Application, answers: Answers): Application = {
    import scala.collection.JavaConversions._
    answers.foreach { case (id, values) =>
      application.setVaiheenVastauksetAndSetPhaseId(id, values)
    }
    application
  }

  def workingAnswers(key: Int) = {
    Map(
      "hakutoiveet" ->
        Map(
          s"preference$key-Koulutus-id" -> "1.2.246.562.14.2013102812460331191879",
          s"preference$key-Opetuspiste-id" -> "1.2.246.562.10.22242122111",
          s"preference$key-Koulutus-id-lang" -> "FI"
        )
    )
  }

  def duplicateAnswers(key: Int) = {
    val answers: Answers = Map(
      "hakutoiveet" ->
        Map(
          s"preference$key-Koulutus-id" -> "1.2.246.562.5.19132087818",
          s"preference$key-Opetuspiste-id" -> "1.2.246.562.10.22242122111",
          s"preference$key-Koulutus-id-lang" -> "FI"
        )
    )
    answers
  }

  def replaceApplicationWishes(answers1: Answers, answers2: Answers): Answers = {
    val answers1ApplicationWishes = answers1.getOrElse("hakutoiveet", Map.empty)
    val answers2ApplicationWishes = answers2.getOrElse("hakutoiveet", Map.empty)
    answers1.updated("hakutoiveet", answers1ApplicationWishes ++ answers2ApplicationWishes)
  }

  def validate[T](hakemus:Hakemus)(f: (List[ValidationError], List[QuestionNode]) => T)(implicit personOid: PersonOid) = {
    authPost("secure/applications/validate/" + hakemus.oid, Serialization.write(hakemus.toHakemusMuutos)) {
      val result: JValue = JsonMethods.parse(body, useBigDecimalForDouble = false)
      val errors: List[ValidationError] = (result \ "errors").extract[List[ValidationError]]
      val structuredQuestions: List[QuestionNode] = (result \ "questions").extract[List[QuestionNode]]
      f(errors, structuredQuestions)
    }
  }

  def validatePut[T, A](hakemus:Hakemus, path: String)(parser: JValue => A)(f: A => T)(implicit personOid: PersonOid) = {
    authPut(s"$path" + hakemus.oid, Serialization.write(hakemus.toHakemusMuutos)) {
      val result: JValue = JsonMethods.parse(body, useBigDecimalForDouble = false)
      val parsedResult: A = parser(result)
      f(parsedResult)
    }
  }
}
