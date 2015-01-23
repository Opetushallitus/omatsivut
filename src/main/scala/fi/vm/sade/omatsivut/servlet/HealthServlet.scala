package fi.vm.sade.omatsivut.servlet

import org.scalatra.{Ok, ScalatraServlet}

class HealthServlet extends ScalatraServlet {
  get("/*") {
    Ok("ok")
  }
}
