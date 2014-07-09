package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.domain.{ValidationError, Hakemus}
import org.json4s.native.Serialization
import fi.vm.sade.omatsivut.servlet.{OmatSivutSwagger, ApplicationsServlet}
import fi.vm.sade.omatsivut.json.JsonFormats

class OHPServletSpec extends JsonFormats with ScalatraTestSupport {
  "GET /applications" should {
    "return person's applications" in {
      authGet("/applications", "1.2.246.562.24.14229104472") {
        verifyApplications(1)
        //verifyOneApplication() TODO FIX
      }
    }
  }

  "POST /application/validate" should {
    "validate application" in {
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

  addServlet(new ApplicationsServlet()(new OmatSivutSwagger), "/*")
}