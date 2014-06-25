package fi.vm.sade.omatsivut

import org.joda.time.DateTime
import org.scalatra.ScalatraBase
import javax.servlet.http.HttpServletRequest

trait Authentication extends ScalatraBase with Logging {

  def oid() = oidOpt.getOrElse(sys.error("Not authenticated account"))

  def oidOpt: Option[String] = parseOid(request) match {
    case Some(cookie) => Some(cookie.oid)
    case _ => None
  }

  private def parseOid(req: HttpServletRequest): Option[CookieCredentials] = {
    authCookie match {
      case Some(c) => {
        try {
          Some(CookieCredentials(AuthenticationCipher.decrypt(c.getValue)))
        } catch {
          case e: Exception => None
        }
      }
      case None => None
    }
  }

  before() {
    oidOpt match {
      case Some(oid) => true
      case None => {
        halt(status = 401, headers = Map("WWW-Authenticate" -> "SecureCookie"))
      }
    }
  }

  def authCookie = {
    for {
      cookies <- Option(request.getCookies)
      auth <- cookies.find((c) => c.getName == "auth")
    } yield auth
  }
}

case class CookieCredentials(oid: String, creationTime: DateTime = new DateTime()) {
  def apply(str: String) = {
    val split = str.split("|").toList
    CookieCredentials(split.head, new DateTime(split.tail.head))
  }
  override def toString = oid + "|" + creationTime.getMillis
}
