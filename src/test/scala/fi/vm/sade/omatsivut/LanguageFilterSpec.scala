package fi.vm.sade.omatsivut

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.domain.Hakemus
import fi.vm.sade.omatsivut.domain.Hakemus.{Hakutoive, Answers}
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.json.JsonFormats
import org.json4s.jackson.Serialization
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import org.scalatra.test.specs2.MutableScalatraSpec
import fi.vm.sade.omatsivut.servlet.LanguageFilter
import fi.vm.sade.omatsivut.domain.Language
import javax.servlet.http.Cookie

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

  "choose with unknown lang param value" should {
    "use fi language" in {
      filter.chooseLanguage(Some("fr"), None)  must_== ((Language.fi, true))
   }
  }

  "choose with en lang param value" should {
    "use en language" in {
      filter.chooseLanguage(Some("en"), None)  must_== ((Language.en, true))
   }
  }

  "choose with sv lang cookie" should {
    "use sv language" in {
      filter.chooseLanguage(None, Some(Array(new Cookie(filter.cookieName,"sv"))))  must_== ((Language.sv, true))
   }
  }

  addFilter(filter, "/*")
}
