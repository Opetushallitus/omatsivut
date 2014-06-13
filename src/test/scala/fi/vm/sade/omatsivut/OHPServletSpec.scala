package fi.vm.sade.omatsivut

import org.scalatra.test.specs2._

class OHPServletSpec extends MutableScalatraSpec {
  "GET / on OHPServlet" should {
    "return status 200" in {
      get("/") {
        status must_== 200
      }
    }
  }

  addServlet(new OHPServlet()(new OHPSwagger), "/*")
}
