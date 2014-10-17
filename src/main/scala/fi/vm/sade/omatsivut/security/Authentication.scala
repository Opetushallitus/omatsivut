package fi.vm.sade.omatsivut.security

import javax.servlet.http.{Cookie, HttpServletRequest, HttpServletResponse}

import fi.vm.sade.omatsivut.auditlog.{AuditLogger, SessionTimeout}
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import fi.vm.sade.omatsivut.util.{Timer, Logging}
import org.joda.time.DateTime
import org.scalatra.ScalatraBase

trait Authentication extends OmatSivutServletBase with AuthCookieParsing with Logging {
  val appConfig: AppConfig
  val authAuditLogger: AuditLogger

  def personOid() = personOidOption(request).getOrElse(sys.error("Unauthenticated account"))

  before() {
    shibbolethCookieInRequest(request) match {
      case Some(cookie) => true
      case None => {
        halt(status = 401, headers = Map("WWW-Authenticate" -> "SecureCookie"))
      }
    }
  }
}

trait AuthCookieParsing extends Logging {
  def appConfig: AppConfig

  def personOidOption(request: HttpServletRequest): Option[String] = {
    if (appConfig.usesFakeAuthentication) {
      FakeAuthentication.fakeOidInRequest(request)
    } else {
      headerOption("oid", request)
    }
  }

  def shibbolethCookieInRequest(req: HttpServletRequest): Option[ShibbolethCookie] = {
    reqCookie(req, c => c.getName.startsWith("_shibsession_")).map(ShibbolethCookie.fromCookie(_))
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
object ShibbolethCookie {
  def fromCookie(cookie: Cookie) = {
    ShibbolethCookie(cookie.getName, cookie.getValue)
  }
  def fromString(str: String): ShibbolethCookie = {
    val split = str.split("=")
    ShibbolethCookie(split(0), split(1))
  }
}

case class AuthInfo() // placeholder. TODO: what to convey to logging?

object FakeAuthentication {
  val oidCookie = "omatsivut-fake-oid"

  def fakeOidInRequest(req: HttpServletRequest): Option[String] = {
    val cookies: List[Cookie] = Option(req.getCookies).map(_.toList).getOrElse(Nil)
    cookies.find(c => c.getName == oidCookie).map(_.getValue).filter(_ != "")
  }
}