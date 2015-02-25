package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.ScalatraTestSupport
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PiwikServletSpec extends ScalatraTestSupport {
  "GET /piwik" should {
    "point to piwik script" in {
      get("piwik/load") {
        status must_== 200
        response.body.indexOf("/wp/wp-content/themes/ophver3/js/piwik.js") >= 0 must beTrue
      }
    }
  }
}
