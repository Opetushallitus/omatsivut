package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.domain._
import fi.vm.sade.omatsivut.fixtures.TestFixture
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
          errors.size must_== 0
          structuredQuestions.size must_== 0
        }

        // TODO: test with added hakutoive -> some questions
        // TODO: test answer validation (pass/fail cases)
        // TODO: test that accepts unknown answers
      }
    }

    "get additional question indices correctly" in {
      withHakemus(TestFixture.hakemus2) { hakemus =>
        val modified = addHakutoive(hevostalous)(hakemus)
        validate(modified) { (errors, structuredQuestions) =>
          QuestionNode.flatten(structuredQuestions).map(_.id) must_== List(QuestionId("hakutoiveet","preference3_kaksoistutkinnon_lisakysymys"))
        }
      }
    }
  }

  def validate[T](hakemus:Hakemus)(f: (List[ValidationError], List[QuestionNode]) => T) = {
    authPost("/applications/validate/" + hakemus.oid, TestFixture.personOid, Serialization.write(hakemus)) {
      status must_== 200
      val result: JValue = JsonMethods.parse(body)
      val errors: List[ValidationError] = (result \ "errors").extract[List[ValidationError]]
      val structuredQuestions: List[QuestionNode] = (result \ "questions").extract[List[QuestionNode]]
      f(errors, structuredQuestions)
    }
  }

  addServlet(new ApplicationsServlet(), "/*")
}
