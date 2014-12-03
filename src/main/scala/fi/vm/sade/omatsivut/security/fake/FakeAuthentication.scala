package fi.vm.sade.omatsivut.security.fake

import javax.servlet.http.{Cookie, HttpServletRequest}
import fi.vm.sade.omatsivut.security.ShibbolethCookie

object FakeAuthentication {
  val oidCookie = "omatsivut-fake-oid"

  def fakeOidInRequest(req: HttpServletRequest): Option[String] = {
    val cookies: List[Cookie] = Option(req.getCookies).map(_.toList).getOrElse(Nil)
    cookies.find(c => c.getName == oidCookie).map(_.getValue).filter(_ != "")
  }

  def authHeaders[A](oid: String): Map[String, String] = {
    val shibbolethCookie: ShibbolethCookie = ShibbolethCookie("_shibsession_test", "test")
    Map("Cookie" -> (oidCookie + "=" + oid + "; " + shibbolethCookie))
  }
}
