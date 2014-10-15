package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.PersonOid
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import fi.vm.sade.omatsivut.hakemus.domain._
import fi.vm.sade.omatsivut.haku.domain.{QuestionId, QuestionNode}
import fi.vm.sade.omatsivut.tarjonta.Hakuaika
import org.json4s._
import org.json4s.jackson.{JsonMethods, Serialization}

class ValidateApplicationSpec extends HakemusApiSpecification with FixturePerson {
  override lazy val appConfig = new AppConfig.IT
  addServlet(componentRegistry.newApplicationsServlet, "/api/applications")

  sequential

  "POST /application/validate" should {
    "validate application" in {
      withHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId) { hakemus =>
        validate(hakemus) { (errors, structuredQuestions, _) =>
          status must_== 200
          errors must_== List()
          structuredQuestions must_== List()
        }
      }
    }

    "reject application with different personOid" in {
      withHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId) { hakemus =>
        validate(hakemus) { (errors, structuredQuestions, _) =>
          status must_== 500
        }(PersonOid("wat"))
      }
    }

    "validate application with extra answers" in {
      val extraQuestionOne: (Hakemus) => Hakemus = answerExtraQuestion(skillsetPhaseKey, "osaaminen-tuntematon-kysymys", "osaaminen-testivastaus")
      val extraQuestionTwo: (Hakemus) => Hakemus = answerExtraQuestion(preferencesPhaseKey, "hakutoive-tuntematon-kysymys", "osaaminen-testivastaus")
      modifyHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId)(extraQuestionOne andThen extraQuestionTwo) { newHakemus =>
        validate(newHakemus) { (errors, structuredQuestions, _) =>
          status must_== 200
          errors must_== List()
          structuredQuestions must_== List()
        }
      }
    }

    "get additional question correctly for old questions" in {
      withHakemus(TestFixture.hakemusWithGradeGridAndDancePreference) { hakemus =>
        validate(hakemus, Some("1.2.246.562.5.31982630126,1.2.246.562.5.68672543292,1.2.246.562.14.2013102812460331191879" )) { (errors, structuredQuestions, _) =>
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

    "return application period of application system if application type is not 'LISÄHAKU'" in {
      withHakemus(hakemusYhteishakuKevat2014WithForeignBaseEducationId) { hakemus =>
        validate(hakemus) { (errors, structuredQuestions, applicationPeriods) =>
          applicationPeriods must_== List(TestFixture.hakemus2_hakuaika)
        }
      }
    }
  }

  def validate[T](hakemus:Hakemus, questionsOf: Option[String] = None)(f: (List[ValidationError], List[QuestionNode], List[Hakuaika]) => T)(implicit personOid: PersonOid) = {
    authPost("/api/applications/validate/" + hakemus.oid + (questionsOf match {
        case Some(value) =>  "?questionsOf=" + value
        case None => ""}), Serialization.write(hakemus.toHakemusMuutos)) {
      val result: JValue = JsonMethods.parse(body)
      val errors: List[ValidationError] = (result \ "errors").extract[List[ValidationError]]
      val structuredQuestions: List[QuestionNode] = (result \ "questions").extract[List[QuestionNode]]
      val applicationPeriods: List[Hakuaika] = (result \ "applicationPeriods").extract[List[Hakuaika]]
      f(errors, structuredQuestions, applicationPeriods)
    }
  }
}
