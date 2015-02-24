package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.ScalatraTestSupport

/**
 *
 */
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
