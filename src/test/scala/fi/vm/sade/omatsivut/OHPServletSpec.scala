package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.domain.{Hakemus, ValidationError}
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.servlet.{ApplicationsServlet, OmatSivutSwagger}
import org.json4s._
import org.json4s.native.{JsonMethods, Serialization}

class OHPServletSpec extends JsonFormats with ScalatraTestSupport {
  sequential

  "GET /applications" should {
    "return person's applications" in {
      AppConfig.fromSystemProperty.withConfig {
        authGet("/applications", "1.2.246.562.24.14229104472") {
          verifyApplications(1)
          //verifyOneApplication() TODO FIX
        }
      }
    }
  }

  "POST /application/validate" should {
    "validate application" in {
      AppConfig.fromSystemProperty.withConfig {
        authGet("/applications", "1.2.246.562.24.14229104472") {
          val applications: List[Hakemus] = Serialization.read[List[Hakemus]](body)
          val hakemus = applications(0)
          authPost("/applications/validate/" + hakemus.oid, "1.2.246.562.24.14229104472", Serialization.write(hakemus)) {
            status must_== 200
            val errors: List[ValidationError] = Serialization.read[List[ValidationError]](body)
            errors.size must_== 3
          }
        }
      }
    }
  }

  "POST /application/unanswered" should {
    "find unanswered question from application" in {
      AppConfig.fromSystemProperty.withConfig {
        authGet("/applications", "1.2.246.562.24.14229104472") {
          val applications: List[Hakemus] = Serialization.read[List[Hakemus]](body)
          val hakemus = applications(0)
          authPost("/applications/unanswered/" + hakemus.oid, "1.2.246.562.24.14229104472", Serialization.write(hakemus)) {
            val questions: JValue = JsonMethods.parse(body)
            val titles: List[String] = questions \\ "title" \\ "fi" \\ classOf[JString]
            questions.children.size must_== 2
            titles.head must_== "Päättötodistuksen kaikkien oppiaineiden keskiarvo?"
            status must_== 200
          }
        }
      }
    }
  }

  def verifyApplications(expectedCount: Int) = {
    val applications: List[Hakemus] = Serialization.read[List[Hakemus]](body)
    status must_== 200
    applications.length must_== expectedCount
  }

  def verifyOneApplication() = {
    val applications: List[Hakemus] = Serialization.read[List[Hakemus]](body)
    val hakemus = applications(0)
    hakemus.oid must_== "1.2.246.562.11.00000876904"
    hakemus.hakutoiveet.length must_== 5
    hakemus.hakutoiveet(0)("Opetuspiste-id") must_== "1.2.246.562.10.60222091211"
  }

  addServlet(new ApplicationsServlet(), "/*")
}