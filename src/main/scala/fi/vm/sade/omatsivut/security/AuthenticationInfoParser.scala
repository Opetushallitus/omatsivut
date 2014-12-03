package fi.vm.sade.omatsivut.security

import javax.servlet.http.HttpServletRequest

import fi.vm.sade.omatsivut.util.Logging

object AuthenticationInfoParser extends Logging {
  def getAuthenticationInfo(request: HttpServletRequest): AuthenticationInfo = {
    val personOid = Option(request.getHeader("oid"))
    val shibbolethCookie = CookieHelper.reqCookie(request, c => c.getName.startsWith("_shibsession_"))
      .map { cookie => ShibbolethCookie(cookie.getName, cookie.getValue) }
    AuthenticationInfo(personOid, shibbolethCookie)
  }
}