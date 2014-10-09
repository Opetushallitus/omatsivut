package fi.vm.sade.omatsivut.security

import javax.servlet.http.{Cookie, HttpServletRequest, HttpServletResponse}

import fi.vm.sade.omatsivut.auditlog.{AuditLogger, SessionTimeout}
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.util.{Timer, Logging}
import org.joda.time.DateTime
import org.scalatra.ScalatraBase

trait Authentication extends ScalatraBase with AuthCookieParsing with Logging {
  val appConfig: AppConfig
  val authAuditLogger: AuditLogger

  def personOid() = personOidOption.getOrElse(sys.error("Unauthenticated account"))

  def personOidOption: Option[String] = credentialsOption(request).flatMap(_.oid)

  def validateCredentials(credentials: CookieCredentials, req: HttpServletRequest) = {
    shibbolethCookieHasNotChanged(credentials, req) && authenticationCookieHasNotTimedOut(credentials)
  }

  private def authenticationCookieHasNotTimedOut(credentials: CookieCredentials): Boolean = {
    credentials.creationTime.plusMinutes(appConfig.cookieTimeoutMinutes).isAfterNow
  }

  private def shibbolethCookieHasNotChanged(credentials: CookieCredentials, req: HttpServletRequest) = {
    Some(credentials.shibbolethCookie) == shibbolethCookieInRequest(req)
  }

  before() {
    credentialsOption(request) match {
      case Some(cookie) if validateCredentials(cookie, request) => true
      case Some(cookie) => {
        logger.info("Cookie was invalid: " + cookie)
        authAuditLogger.log(SessionTimeout(cookie))
        tellBrowserToDeleteAuthCookie(request, response)
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

  def authCookie(req: HttpServletRequest) = {
    reqCookie(req, {_.getName == "auth"})
  }

  def credentialsOption(request: HttpServletRequest): Option[CookieCredentials] = {
    Timer.timed(blockname = "credentialsOption") {
      parseCredentials(request, new AuthenticationCipher(appConfig.settings.aesKey, appConfig.settings.hmacKey))
    }
  }

  private def reqCookie(req: HttpServletRequest, matcher: (Cookie) => Boolean) = {
    for {
      cookies <- Option(req.getCookies)
      cookie <- cookies.find(matcher)
    } yield cookie
  }

  def parseCredentials(req: HttpServletRequest, cipher: AuthenticationCipher): Option[CookieCredentials] = {
    authCookie(req) match {
      case Some(c) => {
        try {
          val decrypt: String = cipher.decrypt(c.getValue)
          Some(CookieCredentials.fromString(decrypt))
        } catch {
          case e: Exception => {
            logger.error("Error while parsing auth cookie", e)
            None
          }
        }
      }
      case None => None
    }
  }

  private def tellBrowserToDeleteCookie(res: HttpServletResponse, cookie: Option[Cookie]) = {
    cookie.map(c => {
      c.setPath("/")
      c.setMaxAge(0)
      res.addCookie(c)
    })
  }

  def tellBrowserToDeleteShibbolethCookie(req: HttpServletRequest, res: HttpServletResponse) {
    tellBrowserToDeleteCookie(res, reqCookie(req, {_.getName.startsWith("_shibsession_")}))
  }

  def tellBrowserToDeleteAuthCookie(req: HttpServletRequest, res: HttpServletResponse){
    tellBrowserToDeleteCookie(res, authCookie(req))
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

object CookieCredentials {
  def fromString(str: String) = {
    val split = str.split("\\|")
    val oidPart: String = split(0)
    val oid = if (oidPart.isEmpty) { None } else { Some(oidPart) }
    CookieCredentials(ShibbolethCookie.fromString(split(1)), oid, new DateTime(split(2).toLong))
  }
}

case class CookieCredentials(shibbolethCookie: ShibbolethCookie, oid: Option[String], creationTime: DateTime = new DateTime()) {
  override def toString = oid.getOrElse("") + "|" + shibbolethCookie.toString + "|" + creationTime.getMillis

  def oidMissing = !hasOid
  def hasOid = oid.isDefined
}

object ShibbolethCookie {
  def fromCookie(cookie: Cookie) = {
    ShibbolethCookie(cookie.getName, cookie.getValue)
  }
  def fromString(str: String) = {
    val split = str.split("=")
    ShibbolethCookie(split(0), split(1))
  }
}
case class ShibbolethCookie(name: String, value: String) {
  override def toString = name + "=" + value
}
