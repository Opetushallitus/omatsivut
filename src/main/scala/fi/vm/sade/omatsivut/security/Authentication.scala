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

  def validateCredentials(credentials: ShibbolethCookie, req: HttpServletRequest) = {
    true
  }

  before() {
    shibbolethCookieInRequest(request) match {
      case Some(cookie) if validateCredentials(cookie, request) => true
      case Some(cookie) => {
        logger.info("Cookie was invalid: " + cookie)
        //authAuditLogger.log(SessionTimeout(cookie)) // TODO: onko relevanttia?
        //tellBrowserToDeleteAuthCookie(request, response)
        halt(status = 401, headers = Map("WWW-Authenticate" -> "SecureCookie"))
      }
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

  private def reqCookie(req: HttpServletRequest, matcher: (Cookie) => Boolean) = {
    for {
      cookies <- Option(req.getCookies)
      cookie <- cookies.find(matcher)
    } yield cookie
  }

  private def tellBrowserToDeleteCookie(res: HttpServletResponse, cookie: Option[Cookie]) = {
    cookie.map(c => {
      c.setPath("/")
      c.setMaxAge(0)
      res.addCookie(c)
    })
  }

  protected def headerOption(name: String, request: HttpServletRequest): Option[String] = {
    Option(request.getHeader(name))
  }

  def tellBrowserToDeleteShibbolethCookie(req: HttpServletRequest, res: HttpServletResponse) {
    tellBrowserToDeleteCookie(res, reqCookie(req, {_.getName.startsWith("_shibsession_")}))
  }

  def shibbolethCookieInRequest(req: HttpServletRequest): Option[ShibbolethCookie] = {
    try {
      val requestCookie = req.getCookies.filter(c => c.getName.startsWith("_shibsession_"))(0)
      Some(ShibbolethCookie.fromCookie(requestCookie))
    } catch {
      case e: Exception => None
    }
  }
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

case class ShibbolethCookie(name: String, value: String) {
  override def toString = name + "=" + value
}

object FakeAuthentication {
  val oidCookie = "omatsivut-fake-oid"

  def fakeOidInRequest(req: HttpServletRequest): Option[String] = {
    req.getCookies.find(c => c.getName == oidCookie).map(_.getValue).filter(_ != "")
  }
}