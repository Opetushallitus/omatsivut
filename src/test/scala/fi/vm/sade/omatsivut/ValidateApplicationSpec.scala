package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.domain.{Hakemus, ValidationError}
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.servlet.ApplicationsServlet
import org.json4s._
import org.json4s.native.{JsonMethods, Serialization}

class ValidateApplicationSpec extends JsonFormats with ScalatraTestSupport {
  sequential


  "POST /application/validate" should {
    "validate application" in {
      println("******************* 2")
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


  addServlet(new ApplicationsServlet(), "/*")
}