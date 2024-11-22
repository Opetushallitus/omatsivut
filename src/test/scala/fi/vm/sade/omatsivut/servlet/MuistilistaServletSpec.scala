//package fi.vm.sade.omatsivut.servlet
//
//import java.net.URLEncoder
//import fi.vm.sade.hakemuseditori.http.UrlValueCompressor
//import fi.vm.sade.omatsivut.ScalatraTestSupport
//import fi.vm.sade.hakemuseditori.json.JsonFormats
//
//import org.junit.runner.RunWith
//import org.specs2.runner.JUnitRunner
//
//@RunWith(classOf[JUnitRunner])
//class MuistilistaServletSpec extends ScalatraTestSupport with JsonFormats {
//
//    "GET muistilista" should {
//      "return 403" in {
//        val exampleBasket: String = """["1.1","1.2"]"""
//        get(s"muistilista/${UrlValueCompressor.compress(exampleBasket)}") {
//          status must_== 403
//        }
//      }
//    }
//
//  "POST muistilista" should {
//    "return HTTP error status 403" in {
//      postJSON("muistilista", """{"otsikko": "otsikko", "kieli": "fi", "vastaanottaja": ["foobar@example.com"], "koids": ["1.2.246.562.14.2013092410023348364157"], "captcha": ""}""") {
//        status must_== 403
//      }
//    }
//  }
//
//
//}
