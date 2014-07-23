package fi.vm.sade.omatsivut.security

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.Logging
import org.joda.time.DateTime
import org.scalatra.ScalatraBase

trait Authentication extends ScalatraBase with AuthCookieParsing with Logging {
  implicit val appConfig: AppConfig

  val cookieTimeoutMinutes = 30

  def oid() = oidOpt.getOrElse(sys.error("Not authenticated account"))

  def oidOpt: Option[String] = credentialsOpt match {
    case Some(cookie) => Some(cookie.oid)
    case _ => None
  }

  def credentialsOpt: Option[CookieCredentials] = {
    parseCredentials(request)
  }

  def validateCredentials(credentials: CookieCredentials, req: HttpServletRequest) = {
    credentials.creationTime.plusMinutes(cookieTimeoutMinutes).isAfterNow
  }

  before() {
    credentialsOpt match {
      case Some(cookie) if validateCredentials(cookie, request) => true
      case Some(cookie) => {
        logger.info("Cookie was invalid: " + cookie)
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
    for {
      cookies <- Option(req.getCookies)
      auth <- cookies.find((c) => c.getName == "auth")
    } yield auth
  }
  
  def parseCredentials(req: HttpServletRequest)(implicit appConfig: AppConfig): Option[CookieCredentials] = {
    authCookie(req) match {
      case Some(c) => {
        try {
          val decrypt: String = AuthenticationCipher().decrypt(c.getValue)
          Some(CookieCredentials.fromString(decrypt))
        } catch {
          case e: Exception => {
            logger.error("parse", e)
            None
          }
        }
      }
      case None => None
    }
  }
  
  def tellBrowserToDeleteAuthCookie(req: HttpServletRequest, res: HttpServletResponse){
    Option(req.getCookies).map(cookies => {
      cookies.find(_.getName == "auth").map(authCookie => {
        authCookie.setPath("/")
        authCookie.setMaxAge(0)
        res.addCookie(authCookie)
      })
    })
  }
}

object CookieCredentials {
  def fromString(str: String) = {
    val split = str.split("\\|")
    CookieCredentials(split(0), split(1), new DateTime(split(2).toLong))
  }
}
case class CookieCredentials(oid: String, shibbolethCookie: String, creationTime: DateTime = new DateTime()) {
  override def toString = oid + "|" + shibbolethCookie + "|" + creationTime.getMillis
}
