package fi.vm.sade.omatsivut.security

import javax.servlet.http.HttpServletRequest

import fi.vm.sade.omatsivut.util.Logging

object AuthenticationInfoParser extends Logging {
  def getAuthenticationInfo(request: HttpServletRequest): AuthenticationInfo = {
    val personOid = Option(request.getHeader("oid"))
    val shibbolethCookie = CookieHelper.reqCookie(request, c => c.getName.startsWith("_shibsession_"))
      .map { cookie => ShibbolethCookie(cookie.getName, cookie.getValue) }
    val error = (shibbolethCookie, personOid, Option(request.getHeader("entitlement"))) match {
      case (Some(_), None, None) => Some("authentication system failure") // <- the "entitlement" header is used to distinguish between "system error" and "oid missing"
      case _ => None
    }
    AuthenticationInfo(personOid, shibbolethCookie, error)
  }
}