package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.domain.Hakemus
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.servlet.ApplicationsServlet
import org.json4s.jackson.Serialization

class GetApplicationsSpec extends JsonFormats with ScalatraTestSupport {
  sequential

  "GET /applications" should {
    "return person's applications" in {
      authGet("/applications", TestFixture.personOid) {
        verifyApplications(3)
        //verifyOneApplication() TODO FIX
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