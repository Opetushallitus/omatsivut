package fi.vm.sade.omatsivut.security

import javax.servlet.http.{Cookie, HttpServletRequest, HttpServletResponse}

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.auditlog.{SessionTimeout, AuditLogger}
import org.joda.time.DateTime
import org.scalatra.ScalatraBase

trait Authentication extends ScalatraBase with AuthCookieParsing with Logging {
  implicit val appConfig: AppConfig

  val cookieTimeoutMinutes = 30

  def personOid() = personOidOption.getOrElse(sys.error("Unauthenticated account"))

  def personOidOption: Option[String] = credentialsOption match {
    case Some(cookie) => Some(cookie.oid)
    case _ => None
  }

  def credentialsOption: Option[CookieCredentials] = {
    parseCredentials(request)
  }

  def validateCredentials(credentials: CookieCredentials, req: HttpServletRequest) = {
    shibbolethCookieHasNotChanged(credentials, req) && authenticationCookieHasNotTimedOut(credentials)
  }

  private def authenticationCookieHasNotTimedOut(credentials: CookieCredentials): Boolean = {
    credentials.creationTime.plusMinutes(cookieTimeoutMinutes).isAfterNow
  }

  private def shibbolethCookieHasNotChanged(credentials: CookieCredentials, req: HttpServletRequest) = {
    Some(credentials.shibbolethCookie) == shibbolethCookieInRequest(req)
  }

  before() {
    credentialsOption match {
      case Some(cookie) if validateCredentials(cookie, request) => true
      case Some(cookie) => {
        logger.info("Cookie was invalid: " + cookie)
        AuditLogger.log(SessionTimeout(cookie))
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

  def authCookie(req: HttpServletRequest) = {
    reqCookie(req, {_.getName == "auth"})
  }

  private def reqCookie(req: HttpServletRequest, matcher: (Cookie) => Boolean) = {
    for {
      cookies <- Option(req.getCookies)
      cookie <- cookies.find(matcher)
    } yield cookie
  }

  def parseCredentials(req: HttpServletRequest)(implicit appConfig: AppConfig): Option[CookieCredentials] = {
    authCookie(req) match {
      case Some(c) => {
        try {
          val decrypt: String = AuthenticationCipher().decrypt(c.getValue)
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
    CookieCredentials(split(0), ShibbolethCookie.fromString(split(1)), new DateTime(split(2).toLong))
  }
}
case class CookieCredentials(oid: String, shibbolethCookie: ShibbolethCookie, creationTime: DateTime = new DateTime()) {
  override def toString = oid + "|" + shibbolethCookie.toString + "|" + creationTime.getMillis
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
