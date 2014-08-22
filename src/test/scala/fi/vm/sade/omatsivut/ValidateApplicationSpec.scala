package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.domain._
import fi.vm.sade.omatsivut.fixtures.{FixtureImporter, TestFixture}
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import fi.vm.sade.omatsivut.servlet.ApplicationsServlet
import org.json4s._
import org.json4s.jackson.{JsonMethods, Serialization}

class ValidateApplicationSpec extends HakemusApiSpecification {
  override implicit lazy val appConfig = new AppConfig.IT
  sequential

  "POST /application/validate" should {
    "validate application" in {
      withHakemus(TestFixture.hakemus1) { hakemus =>
        validate(hakemus) { (errors, structuredQuestions) =>
          errors must_== List()
          structuredQuestions must_== List()
        }
      }
    }

    "validate application with extra answers" in {
      val extraQuestionOne: (Hakemus) => Hakemus = answerExtraQuestion(skillsetPhaseKey, "osaaminen-tuntematon-kysymys", "osaaminen-testivastaus")
      val extraQuestionTwo: (Hakemus) => Hakemus = answerExtraQuestion(preferencesPhaseKey, "hakutoive-tuntematon-kysymys", "osaaminen-testivastaus")
      modifyHakemus(hakemus1)(extraQuestionOne andThen extraQuestionTwo) { newHakemus =>
        validate(newHakemus) { (errors, structuredQuestions) =>
          errors must_== List()
          structuredQuestions must_== List()
        }
      }
    }

    "add additional questions related to added preference" in {
      FixtureImporter().applyFixtures("peruskoulu")
      withHakemus(TestFixture.hakemus2) { hakemus =>
        val modified = addHakutoive(ammattistarttiAhlman)(hakemus)
        validate(modified) { (errors, structuredQuestions) =>
          QuestionNode.flatten(structuredQuestions).map(_.id) must_== List(
            QuestionId("hakutoiveet","preference3-discretionary"),
            QuestionId("lisatiedot","TYOKOKEMUSKUUKAUDET")
          )
        }
      }
    }

    "get additional question correctly for old questions" in {
      withHakemus(TestFixture.hakemusWithGradeGridAndDancePreference) { hakemus =>
        validate(hakemus, Some("1.2.246.562.5.31982630126,1.2.246.562.5.68672543292,1.2.246.562.14.2013102812460331191879" )) { (errors, structuredQuestions) =>
          QuestionNode.flatten(structuredQuestions).map(_.id) must_== List(
             QuestionId("hakutoiveet","preference1-discretionary"),
             QuestionId("hakutoiveet","preference1_kaksoistutkinnon_lisakysymys"),
             QuestionId("lisatiedot","TYOKOKEMUSKUUKAUDET"),
             QuestionId("hakutoiveet","preference2-discretionary"),
             QuestionId("hakutoiveet","preference2_urheilijan_ammatillisen_koulutuksen_lisakysymys"),
             QuestionId("hakutoiveet","preference2_kaksoistutkinnon_lisakysymys"),
             QuestionId("lisatiedot","TYOKOKEMUSKUUKAUDET"),
             QuestionId("hakutoiveet","preference3-discretionary"),
             QuestionId("lisatiedot","TYOKOKEMUSKUUKAUDET")
          )
        }
      }
    }
  }

  def validate[T](hakemus:Hakemus, questionsOf: Option[String] = None)(f: (List[ValidationError], List[QuestionNode]) => T) = {
    authPost("/applications/validate/" + hakemus.oid + (questionsOf match {
        case Some(value) =>  "?questionsOf=" + value
        case None => ""}),
        TestFixture.personOid, Serialization.write(hakemus)) {
      status must_== 200
      val result: JValue = JsonMethods.parse(body)
      val errors: List[ValidationError] = (result \ "errors").extract[List[ValidationError]]
      val structuredQuestions: List[QuestionNode] = (result \ "questions").extract[List[QuestionNode]]
      f(errors, structuredQuestions)
    }
  }

  addServlet(new ApplicationsServlet(), "/*")
}
