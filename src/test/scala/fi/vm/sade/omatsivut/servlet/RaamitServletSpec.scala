//package fi.vm.sade.omatsivut.servlet
//
//import fi.vm.sade.omatsivut.ScalatraTestSupport
//import org.junit.runner.RunWith
//import org.specs2.runner.JUnitRunner
//
//@RunWith(classOf[JUnitRunner])
//class RaamitServletSpec extends ScalatraTestSupport  {
//  "GET /raamit" should {
//    "point to oppija-raamit" in {
//      get("raamit/load") {
//        status must_== 200
//        response.body.contains("/oppija-raamit/js/apply-raamit.js") must beTrue
//      }
//    }
//  }
//}
