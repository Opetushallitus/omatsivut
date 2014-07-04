package fi.vm.sade.omatsivut

import org.scalatra.test.specs2.MutableScalatraSpec
import fi.vm.sade.omatsivut.servlet.SessionServlet

class SessionServletSpec extends MutableScalatraSpec {

  "GET /secure/initsession" should {
    "generate auth cookie" in {
      get("/secure/initsession", headers = Map("Hetu" -> "010101-123N")) {
        status must_== 302
        val setCookie = response.headers("Set-Cookie")(0)
        val encrypted = setCookie.substring(setCookie.indexOf('='), setCookie.indexOf(';') + 1)
        encrypted must_!= ""
      }
    }
  }

  addServlet(new SessionServlet()(AppConfig.IT.authenticationInfoService), "/secure")
}
