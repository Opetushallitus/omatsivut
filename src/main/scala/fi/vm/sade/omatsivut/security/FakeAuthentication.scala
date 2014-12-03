package fi.vm.sade.omatsivut.security

import javax.servlet.http.{Cookie, HttpServletRequest}

object FakeAuthentication {
  val oidCookie = "omatsivut-fake-oid"

  def fakeOidInRequest(req: HttpServletRequest): Option[String] = {
    val cookies: List[Cookie] = Option(req.getCookies).map(_.toList).getOrElse(Nil)
    cookies.find(c => c.getName == oidCookie).map(_.getValue).filter(_ != "")
  }
}
