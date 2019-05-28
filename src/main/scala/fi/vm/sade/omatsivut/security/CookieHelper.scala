package fi.vm.sade.omatsivut.security

import java.net.HttpCookie

import javax.servlet.http.{Cookie, HttpServletRequest}

object CookieHelper {
  def reqCookie(req: HttpServletRequest, matcher: (Cookie) => Boolean) = {
    for {
      cookies <- Option(req.getCookies)
      cookie <- cookies.find(matcher)
    } yield cookie
  }

  def getCookie(req: HttpServletRequest, cookieName: String): Option[Cookie] = {
    reqCookie(req, (c) => c.getName == cookieName)
  }

  def cookieExtractValue(cookieString: String, cookieName: String): Option[String] = {
    val cookieComponents = cookieString.split(Array('=', ';'))
    if (cookieComponents.length > 2) {
      if (cookieComponents(0) == cookieName) Some(cookieComponents(1)) else None
    }
    else None
  }

  /**
    * Helper to create a headers map with the cookies specified. Merge with another map for more headers.
    *
    * This allows only basic cookies, no expiry or domain set.
    *
    * @param cookies key-value pairs
    * @return a map suitable for passing to a get() or post() Scalatra test method
    */
  def cookieHeaderWith(cookies: Map[String, String]): Map[String, String] = {
    val asCookies = cookies.map { case (k, v) => new HttpCookie(k, v) }
    val headerValue = asCookies.mkString("; ")
    Map("Cookie" -> headerValue)
  }

  /**
    * Syntatically nicer function for cookie header creation:
    *
    * cookieHeaderWith("testcookie" -> "what")
    *
    * instead of
    * cookieHeaderWith(Map("testcookie" -> "what"))
    *
    */
  def cookieHeaderWith(cookies: (String, String)*): Map[String, String] = {
    cookieHeaderWith(cookies.toMap)
  }
}
