package fi.vm.sade.omatsivut

import org.joda.time.DateTime
import org.scalatra.{CookieOptions, Cookie, ScalatraBase}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import org.slf4j.LoggerFactory

trait Authentication extends ScalatraBase with AuthCookieParsing with Logging {
  val cookieTimeoutMinutes = 30

  def oid() = oidOpt.getOrElse(sys.error("Not authenticated account"))

  def oidOpt: Option[String] = credentialsOpt match {
    case Some(cookie) => Some(cookie.oid)
    case _ => None
  }

  def credentialsOpt: Option[CookieCredentials] = {
    parseCredentials(request)
  }

  before() {
    credentialsOpt match {
      case Some(cookie) if cookie.creationTime.plusMinutes(cookieTimeoutMinutes).isAfterNow => true
      case Some(cookie) => {
        logger.info("Cookie timed out: " + cookie)
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
  
  def parseCredentials(req: HttpServletRequest): Option[CookieCredentials] = {
    authCookie(req) match {
      case Some(c) => {
        try {
          val decrypt: String = AuthenticationCipher.decrypt(c.getValue)
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
    val authCookie = req.getCookies.find(_.getName == "auth").get
    authCookie.setMaxAge(0)
    res.addCookie(authCookie)
  }
}

object CookieCredentials {
  def fromString(str: String) = {
    val split = str.split("\\|")
    CookieCredentials(split(0), new DateTime(split(1).toLong))
  }
}
case class CookieCredentials(oid: String, creationTime: DateTime = new DateTime()) {
  override def toString = oid + "|" + creationTime.getMillis
}
