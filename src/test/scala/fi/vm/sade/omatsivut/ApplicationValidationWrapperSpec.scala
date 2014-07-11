package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.domain.Hakemus
import fi.vm.sade.omatsivut.hakemus.ApplicationValidationWrapper
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.servlet.{OmatSivutSwagger, ApplicationsServlet}
import org.json4s.native.Serialization

class ApplicationValidationWrapperSpec extends JsonFormats with ScalatraTestSupport {
  "ApplicationValidationWrapper" should {
    "be able to find missing elements" in {
      authGet("/applications", "1.2.246.562.24.14229104472") {
        val applications: List[Hakemus] = Serialization.read[List[Hakemus]](body)
        val hakemus = applications(0)
        val missingAnswers = ApplicationValidationWrapper.findMissingElements(hakemus)
        missingAnswers.size must_== 2
      }
    }
  }

  addServlet(new ApplicationsServlet()(new OmatSivutSwagger), "/*")
}
