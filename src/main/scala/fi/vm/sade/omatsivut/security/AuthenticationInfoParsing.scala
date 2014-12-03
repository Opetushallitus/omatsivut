package fi.vm.sade.omatsivut.security

import javax.servlet.http.{Cookie, HttpServletRequest}
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.util.Logging

trait AuthenticationInfoParsing extends Logging {
  def appConfig: AppConfig

  def personOidOption(request: HttpServletRequest): Option[String] = {
    headerOption("oid", request)
  }

  def shibbolethCookieInRequest(req: HttpServletRequest): Option[ShibbolethCookie] = {
    reqCookie(req, c => c.getName.startsWith("_shibsession_"))
      .map { cookie => ShibbolethCookie(cookie.getName, cookie.getValue) }
  }

  def authInfo(request: HttpServletRequest) = {
    AuthenticationInfo(personOidOption(request), shibbolethCookieInRequest(request))
  }

  protected def headerOption(name: String, request: HttpServletRequest): Option[String] = {
    Option(request.getHeader(name))
  }

  protected def reqCookie(req: HttpServletRequest, matcher: (Cookie) => Boolean) = {
    for {
      cookies <- Option(req.getCookies)
      cookie <- cookies.find(matcher)
    } yield cookie
  }
}

case class ShibbolethCookie(name: String, value: String) {
  override def toString = name + "=" + value
}

case class AuthenticationInfo(personOid: Option[String], shibbolethCookie: Option[ShibbolethCookie]) {
  override def toString = "oid=" + personOid.getOrElse("") + ", " + shibbolethCookie.map(_.toString).getOrElse("(no shibboleth cookie)")
}