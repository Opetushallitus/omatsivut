package fi.vm.sade.omatsivut.security.fake

import javax.servlet.http.{Cookie, HttpServletRequest}
import fi.vm.sade.omatsivut.security.{CookieHelper, ShibbolethCookie}

object FakeAuthentication {
  val fakeCookiePrefix = "omatsivut-fake-"

  def authHeaders[A](oid: String): Map[String, String] = {
    val shibbolethCookie: ShibbolethCookie = ShibbolethCookie("_shibsession_test", "test")
    Map("Cookie" -> (fakeCookiePrefix + "oid=" + oid + "; " + shibbolethCookie))
  }
}
