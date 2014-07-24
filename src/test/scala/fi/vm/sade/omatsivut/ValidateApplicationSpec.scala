package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.domain._
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.servlet.ApplicationsServlet
import org.json4s._
import org.json4s.jackson.{JsonMethods, Serialization}

class ValidateApplicationSpec extends HakemusApiSpecification {
  override implicit lazy val appConfig = new AppConfig.IT
  sequential

  "POST /application/validate" should {
    "validate application" in {
      withHakemus { hakemus =>
        authPost("/applications/validate/" + hakemus.oid, TestFixture.personOid, Serialization.write(hakemus)) {
          status must_== 200
          val result: JValue = JsonMethods.parse(body)
          val errors: List[ValidationError] = (result \ "errors").extract[List[ValidationError]]
          errors.size must_== 0
          val structuredQuestions: List[QuestionNode] = (result \ "questions").extract[List[QuestionNode]]
          structuredQuestions.size must_== 0
        }

        // TODO: test with added hakutoive -> some questions
        // TODO: test answer validation (pass/fail cases)
        // TODO: test that accepts unknown answers
      }
    }
  }

  addServlet(new ApplicationsServlet(), "/*")
}
