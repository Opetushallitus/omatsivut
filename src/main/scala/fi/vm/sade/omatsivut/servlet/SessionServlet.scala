package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.security._
import org.scalatra.{Cookie, CookieOptions}
import org.scalatra.servlet.RichResponse

import scala.collection.JavaConverters._

class SessionServlet(implicit val appConfig: AppConfig) extends OmatSivutServletBase with AuthCookieParsing {
  get("/initsession") {
    request.getHeaderNames.asScala.toList.map(h => logger.info(h + ": " + request.getHeader(h)))
    createAuthCookieCredentials match {
      case Some(credentials) => createAuthCookieResponse(credentials)
      case _ => response.redirect(ssoContextPath + "/Shibboleth.sso/LoginFI") //TODO Localization
    }
  }

  protected def findHetuFromParams = {
    headerOption("Hetu") match {
      case Some(hetu) => Some(hetu)
      case None if appConfig.isTest => paramOption("hetu")
      case _ => None
    }
  }

  protected def makeCookieOptions = if (appConfig.isTest) CookieOptions(path = "/") else CookieOptions(secure = true, path = "/", maxAge = 1799)

  protected def ssoContextPath: String = if (appConfig.isTest) "/omatsivut" else "/"

  private def createAuthCookieCredentials: Option[CookieCredentials] = {
    checkCredentials match {
      case Some((oid, cookie)) => Some(CookieCredentials(oid, cookie))
      case _ => None
    }
  }

  private def checkCredentials = {
    for {
      hetu <- findHetuFromParams
      cookie <- shibbolethCookieInRequest(request)
      oid <- AuthenticationInfoService.apply.getHenkiloOID(hetu)
    } yield (oid, cookie)
  }

  private def createAuthCookieResponse(credentials: CookieCredentials) {
    val encryptedCredentials = AuthenticationCipher().encrypt(credentials.toString)
    response.addCookie(Cookie("auth", encryptedCredentials)(makeCookieOptions))
    logger.info("Redirecting to " + redirectUri)
    response.redirect(redirectUri)
  }

  private def redirectUri: String = {
    request.getContextPath + paramOption("redirect").getOrElse("/index.html")
  }

  get("/logout") {
    tellBrowserToDeleteAuthCookie(request, response)
    response.redirect(ssoContextPath + "/Shibboleth.sso/Logout?type=Local")
  }
}