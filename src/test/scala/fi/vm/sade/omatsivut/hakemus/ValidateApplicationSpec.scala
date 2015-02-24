package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.hakemuseditori.hakemus.domain._
import fi.vm.sade.hakemuseditori.lomake.domain.{QuestionId, QuestionNode}
import fi.vm.sade.omatsivut.PersonOid
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import org.json4s._
import org.json4s.jackson.{JsonMethods, Serialization}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ValidateApplicationSpec extends HakemusApiSpecification with FixturePerson {
  override lazy val appConfig = new AppConfig.IT
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
      withHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId) { hakemusInfo =>
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
      withHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId) { hakemusInfo =>
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
      withHakemus(TestFixture.hakemusWithGradeGridAndDancePreference) { hakemusInfo =>
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
  }

  def validate[T](hakemus:Hakemus)(f: (List[ValidationError], List[QuestionNode]) => T)(implicit personOid: PersonOid) = {
    authPost("secure/applications/validate/" + hakemus.oid, Serialization.write(hakemus.toHakemusMuutos)) {
      val result: JValue = JsonMethods.parse(body)
      val errors: List[ValidationError] = (result \ "errors").extract[List[ValidationError]]
      val structuredQuestions: List[QuestionNode] = (result \ "questions").extract[List[QuestionNode]]
      f(errors, structuredQuestions)
    }
  }
}