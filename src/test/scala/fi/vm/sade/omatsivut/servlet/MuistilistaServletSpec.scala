package fi.vm.sade.omatsivut.servlet

import java.net.URLEncoder

import fi.vm.sade.omatsivut.ScalatraTestSupport
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.http.UrlValueCompressor
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.muistilista.Muistilista
import org.json4s.jackson.Serialization


class MuistilistaServletSpec extends ScalatraTestSupport with JsonFormats {

  override lazy val appConfig = new AppConfig.IT

  "GET muistilista" should {
    "make a cookie with correct ids" in {
      val exampleBasket: String = """["1.1","1.2"]"""
      get(s"muistilista/${UrlValueCompressor.compress(exampleBasket)}") {
        val basketCookieAsString: String = response.headers("Set-Cookie").filter(_.startsWith("basket")).head
        basketCookieAsString must_== s"basket=${URLEncoder.encode(exampleBasket, "UTF-8")};Path=/"
        status must_== 200
      }
    }
  }

  "POST muistilista" should {
    "palauttaa koostettavan emailin" in {
      postJSON("muistilista", Serialization.write(Muistilista(Some("lahettaja"), "otsikko", Language.fi, List("foobar@example.com"), List("1.2.246.562.20.94964838901")))) {
        status must_== 200
        println("body="+body)
        body.isEmpty must_== false
      }
    }
  }

}
