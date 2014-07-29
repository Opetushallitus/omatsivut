package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.security._
import org.scalatra.{Cookie, CookieOptions}
import org.scalatra.servlet.RichResponse
import scala.collection.JavaConverters._
import fi.vm.sade.omatsivut.auditlog.AuditLogger

class SessionServlet(implicit val appConfig: AppConfig) extends OmatSivutServletBase with AuthCookieParsing {
  get("/initsession") {
    request.getHeaderNames.asScala.toList.map(h => logger.info(h + ": " + request.getHeader(h)))
    checkCredentials match {
      case (Some(oid), Some(cookie)) => {
        val credentials: CookieCredentials = CookieCredentials(oid, cookie)
        AuditLogger.logCreateSession(credentials)
        createAuthCookieResponse(credentials)
      }
      case (None, Some(cookie)) => {
        logger.warn("No user OID found. Cookie: " + cookie)
        resourceNotFound()
      }
      case _ => response.redirect(appConfig.authContext.ssoContextPath + "/Shibboleth.sso/LoginFI") //TODO Localization
    }
  }

  private def findHetuFromParams = {
    headerOption("Hetu") match {
      case Some(hetu) => Some(hetu)
      case None if appConfig.usesFakeAuthentication => paramOption("hetu")
      case _ => None
    }
  }

  private def checkCredentials: (Option[String], Option[ShibbolethCookie]) = {
    val oid = for {
      hetu <- findHetuFromParams
      oid <- AuthenticationInfoService.apply.getHenkiloOID(hetu)
    } yield oid
    (oid, shibbolethCookieInRequest(request))
  }

  private def createAuthCookieResponse(credentials: CookieCredentials) {
    val encryptedCredentials = AuthenticationCipher().encrypt(credentials.toString)
    response.addCookie(Cookie("auth", encryptedCredentials)(appConfig.authContext.cookieOptions))
    logger.info("Redirecting to " + redirectUri)
    response.redirect(redirectUri)
  }

  private def redirectUri: String = {
    request.getContextPath + paramOption("redirect").getOrElse("/index.html")
  }
}