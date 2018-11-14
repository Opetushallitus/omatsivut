package fi.vm.sade.omatsivut.security

import javax.servlet.http.HttpServletRequest

import fi.vm.sade.utils.slf4j.Logging

object AuthenticationInfoParser extends Logging with CookieNames {
  def getAuthenticationInfo(request: HttpServletRequest): AuthenticationInfo = {
    val personOid = CookieHelper.getCookie(request, oppijaNumeroCookieName)
    val oppijaNumero = CookieHelper.getCookie(request, sessionCookieName)
    AuthenticationInfo(personOid.map(_.getValue), oppijaNumero.map(_.getValue))
  }
}
