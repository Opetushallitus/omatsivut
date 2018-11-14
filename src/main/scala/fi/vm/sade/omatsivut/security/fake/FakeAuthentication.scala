package fi.vm.sade.omatsivut.security.fake

import fi.vm.sade.omatsivut.security.CookieNames
import javax.servlet.http.{Cookie, HttpServletRequest}

object FakeAuthentication extends CookieNames {

  def fakeOidInRequest(req: HttpServletRequest): Option[String] = {
    val cookies: List[Cookie] = Option(req.getCookies).map(_.toList).getOrElse(Nil)
    cookies.find(c => c.getName == oppijaNumeroCookieName).map(_.getValue).filter(_ != "")
  }

  def authHeaders[A](oid: String, sessionId: String): Map[String, String] = {
    Map("Cookie" -> (oppijaNumeroCookieName + "=" + oid + "; " + sessionCookieName + "=" + sessionId))
  }
}
