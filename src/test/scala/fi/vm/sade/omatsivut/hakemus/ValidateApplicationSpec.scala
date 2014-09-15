package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import fi.vm.sade.omatsivut.hakemus.domain._
import fi.vm.sade.omatsivut.haku.domain.{QuestionId, QuestionNode, HakuAika}
import org.json4s._
import org.json4s.jackson.{JsonMethods, Serialization}

class ValidateApplicationSpec extends HakemusApiSpecification {
  override implicit lazy val appConfig = new AppConfig.IT
  addServlet(appConfig.componentRegistry.newApplicationsServlet, "/*")

  sequential

  "POST /application/validate" should {
    "validate application" in {
      withHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId) { hakemus =>
        validate(hakemus) { (errors, structuredQuestions, _) =>
          errors must_== List()
          structuredQuestions must_== List()
        }
      }
    }

    "validate application with extra answers" in {
      val extraQuestionOne: (Hakemus) => Hakemus = answerExtraQuestion(skillsetPhaseKey, "osaaminen-tuntematon-kysymys", "osaaminen-testivastaus")
      val extraQuestionTwo: (Hakemus) => Hakemus = answerExtraQuestion(preferencesPhaseKey, "hakutoive-tuntematon-kysymys", "osaaminen-testivastaus")
      modifyHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId)(extraQuestionOne andThen extraQuestionTwo) { newHakemus =>
        validate(newHakemus) { (errors, structuredQuestions, _) =>
          errors must_== List()
          structuredQuestions must_== List()
        }
      }
    }

    "get additional question correctly for old questions" in {
      withHakemus(TestFixture.hakemusWithGradeGridAndDancePreference) { hakemus =>
        validate(hakemus, Some("1.2.246.562.5.31982630126,1.2.246.562.5.68672543292,1.2.246.562.14.2013102812460331191879" )) { (errors, structuredQuestions, _) =>
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

    "return application period of first preference if application type is 'LISÄHAKU'" in {
      withHakemus(TestFixture.hakemusLisahaku) { hakemus =>
        validate(hakemus) { (errors, structuredQuestions, applicationPeriods) =>
          applicationPeriods must_== List(TestFixture.hakemusLisahaku_hakuaikaForPreference)
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

    "update application period when the first preference is changed in 'LISÄHAKU'" in {
      withHakemus(TestFixture.hakemusLisahaku) { hakemus =>
        val modified = addHakutoive(TestFixture.ammattistarttiAhlman)(removeHakutoive(hakemus))
        validate(modified) { (_, _, applicationPeriods) =>
          applicationPeriods must_== List(TestFixture.hakemusLisahaku_hakuaikaDefault)
        }
      }
    }
  }

  def validate[T](hakemus:Hakemus, questionsOf: Option[String] = None)(f: (List[ValidationError], List[QuestionNode], List[HakuAika]) => T) = {
    authPost("/applications/validate/" + hakemus.oid + (questionsOf match {
        case Some(value) =>  "?questionsOf=" + value
        case None => ""}),
        TestFixture.personOid, Serialization.write(hakemus.toHakemusMuutos)) {
      status must_== 200
      val result: JValue = JsonMethods.parse(body)
      val errors: List[ValidationError] = (result \ "errors").extract[List[ValidationError]]
      val structuredQuestions: List[QuestionNode] = (result \ "questions").extract[List[QuestionNode]]
      val applicationPeriods: List[HakuAika] = (result \ "applicationPeriods").extract[List[HakuAika]]
      f(errors, structuredQuestions, applicationPeriods)
    }
  }
}
