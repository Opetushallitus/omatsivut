package fi.vm.sade.omatsivut

import org.json4s.native.Serialization

class OHPServletSpec extends OHPJsonFormats with TestSupport {

  "GET /applications" should {
    "return person's applications" in {
      authGet("/applications", "1.2.246.562.24.14229104472") {
        verifyApplications(1)
        //verifyOneApplication() TODO FIX
      }
    }

    "return 401 if not authenticated" in {
      get("/applications") {
        status must_== 401
      }
    }
  }

  def verifyApplications(expectedCount: Int) = {
    val applications: List[Hakemus] = Serialization.read[List[Hakemus]](body)
    applications.length must_== expectedCount
    status must_== 200
  }

  def verifyOneApplication() = {
    val applications: List[Hakemus] = Serialization.read[List[Hakemus]](body)
    val hakemus = applications(0)
    hakemus.oid must_== "1.2.246.562.11.00000876904"
    hakemus.hakutoiveet.length must_== 5
    hakemus.hakutoiveet(0)("Opetuspiste-id") must_== "1.2.246.562.10.60222091211"
  }

  addServlet(new OHPServlet()(new OHPSwagger), "/*")
}