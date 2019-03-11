package fi.vm.sade.omatsivut.security

import javax.servlet.http.{Cookie, HttpServletRequest}
import fi.vm.sade.utils.slf4j.Logging

object AuthenticationInfoParser extends Logging with CookieNames {
  def getAuthenticationInfo(request: HttpServletRequest): AuthenticationInfo = {
    def valOrNoneIfEmpty(value: Option[String]): Option[String] = {
      value.find(!_.isEmpty())
    }

    val personOidCookie: Option[Cookie] = CookieHelper.getCookie(request, oppijaNumeroCookieName)
    val personOid: Option[String] = personOidCookie.map(_.getValue)
    val sessionIdCookie: Option[Cookie] = CookieHelper.getCookie(request, sessionCookieName)
    val sessionId: Option[String] = sessionIdCookie.map(_.getValue)
    val a = AuthenticationInfo(valOrNoneIfEmpty(personOid), valOrNoneIfEmpty(sessionId))
    a
  }
}
