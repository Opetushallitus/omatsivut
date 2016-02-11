package fi.vm.sade.omatsivut.servlet

import javax.servlet.http.Cookie

import fi.vm.sade.hakemuseditori.domain.Language
import org.scalatra.test.specs2.MutableScalatraSpec

class LanguageFilterSpec extends MutableScalatraSpec {
  val filter = new LanguageFilter()

  "GET / with lang sv" should {
    "Set lang cookie" in {
      get("/?lang=sv") {
        status must_== 200
        val setCookie = response.headers("Set-Cookie")(0)
        setCookie.indexOf(filter.cookieName + "=sv") >= 0 must beTrue
      }
    }
  }

  "GET / with unknown lang" should {
    "set default language fi" in {
      get("/?lang=no") {
        status must_== 200
        val setCookie = response.headers("Set-Cookie")(0)
        setCookie.indexOf(filter.cookieName + "=fi") >= 0 must beTrue
      }
    }
  }

  /*"choose with unknown lang param value" should {
    "use fi language" in {
      filter.chooseLanguage(Some("fr"), None, "http://localhost:7337/")  must_== ((Language.fi, true))
   }
  }

  "choose with en lang param value" should {
    "use en language" in {
      filter.chooseLanguage(Some("en"), None, "http://localhost:7337/")  must_== ((Language.en, true))
   }
  }

  "choose with sv lang cookie" should {
    "use sv language" in {
      filter.chooseLanguage(None, Some(Array(new Cookie(filter.cookieName,"sv"))), "http://localhost:7337/")  must_== ((Language.sv, true))
   }
  }

  "choose with studieinfo domain" should {
    "use sv language" in {
      filter.chooseLanguage(None, None, "https://testi.studieinfo.fi/omatsivut/hakutoiveidenMuokkaus.html#/")  must_== ((Language.sv, true))
    }
  }

  "choose with unknown domain" should {
    "use fi language" in {
      filter.chooseLanguage(None, None, "http://localhost:7337/")  must_== ((Language.fi, true))
    }
  }*/

  addFilter(filter, "/*")
}
