package fi.vm.sade.omatsivut.security

import javax.servlet.http.{Cookie, HttpServletRequest}

object CookieHelper {
  def reqCookie(req: HttpServletRequest, matcher: (Cookie) => Boolean) = {
    for {
      cookies <- Option(req.getCookies)
      cookie <- cookies.find(matcher)
    } yield cookie
  }
}
