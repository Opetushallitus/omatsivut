package fi.vm.sade.omatsivut.servlet

import org.scalatra.test.specs2.MutableScalatraSpec

class HealthServletSpec extends MutableScalatraSpec {
  addServlet(classOf[HealthServlet], "/health")

  "/health" should {
    "return 200" in {
      get("/health") {
        status must_== 200
      }
    }
  }
}
