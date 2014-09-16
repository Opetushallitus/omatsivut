package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.ScalatraTestSupport

class RaamitServletSpec extends ScalatraTestSupport  {
  "GET /raamit" should {
    "point to oppija-raamit" in {
      get("/raamit/load") {
        status must_== 200
        response.body.indexOf("/oppija-raamit/apply-raamit.js") >= 0 must beTrue
      }
    }
  }

  addServlet(new RaamitServlet(appConfig), "/*")
}
