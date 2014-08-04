package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.servlet.NaviServlet

class NaviServletSpec extends ScalatraTestSupport  {

  "GET /navi" should {
    "point to localhost oppija-raamit" in {
      get("/navi/load") {
        status must_== 200
        response.body.indexOf("http://localhost:8099/oppija-raamit/apply-raamit.js") >= 0 must beTrue
      }
    }
  }

  addServlet(new NaviServlet(), "/*")
}
