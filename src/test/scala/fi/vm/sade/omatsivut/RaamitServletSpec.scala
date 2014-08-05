package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.servlet.RaamitServlet

class RaamitServletSpec extends ScalatraTestSupport  {

  "GET /raamit" should {
    "point to localhost oppija-raamit" in {
      get("/raamit/load") {
        status must_== 200
        response.body.indexOf("http://localhost:8099/oppija-raamit/apply-raamit.js") >= 0 must beTrue
      }
    }
  }

  addServlet(new RaamitServlet(), "/*")
}
