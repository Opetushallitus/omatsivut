package fi.vm.sade.omatsivut

import org.scalatra.ScalatraBase
import javax.servlet.http.HttpServletRequest

trait Authentication extends ScalatraBase with Logging {

  def oid() = oidOpt.getOrElse(sys.error("Not authenticated account"))

  def oidOpt: Option[String] = parseOid(request)

  private def parseOid(req: HttpServletRequest): Option[String] = {
    val auth = for {
      cookies <- Option(request.getCookies)
      auth <- cookies.find((c) => c.getName == "auth")
    } yield auth
    auth match {
      case Some(c) => {
        try {
          Some(AuthenticationCipher.decrypt(c.getValue))
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
}

