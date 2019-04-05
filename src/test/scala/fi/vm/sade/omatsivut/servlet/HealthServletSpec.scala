package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.ScalatraTestSupport
import org.scalatra.test.specs2.MutableScalatraSpec

class HealthServletSpec extends ScalatraTestSupport {
  "/health" should {
    "return 200" in {
      get("health") {
        status must_== 200
      }
    }
  }
}
