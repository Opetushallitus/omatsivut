package fi.vm.sade.omatsivut

import org.scalatra.test.specs2._
import org.json4s.native.Serialization

class OHPServletSpec extends MutableScalatraSpec with OHPJsonFormats {
  "GET / on OHPServlet" should {
    "return status 200" in {
      get("/") {
        status must_== 200
      }
    }
  }

  "GET /applications/:hetu" should {
    "return person's applications" in {
      get("/applications/010101-123N") {
        verifyApplications(1)
        verifyOneApplication()
      }
    }

    "return 404 if person not found" in {
      get("/applications/130694-9537") {
        status must_== 404
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
