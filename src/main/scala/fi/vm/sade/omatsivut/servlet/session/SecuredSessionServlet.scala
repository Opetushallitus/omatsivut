package fi.vm.sade.omatsivut.servlet.session

import fi.vm.sade.omatsivut.config.AppConfig
import AppConfig.AppConfig
import fi.vm.sade.omatsivut.auditlog.{AuditLogger, Login}
import fi.vm.sade.omatsivut.security._
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import org.scalatra.Cookie

import scala.collection.JavaConverters._

class SecuredSessionServlet(implicit val appConfig: AppConfig) extends OmatSivutServletBase with AuthCookieParsing with ShibbolethPaths {
  get("/initsession") {
    checkCredentials match {
      case (Some(oid), Some(cookie)) => {
        val credentials: CookieCredentials = CookieCredentials(oid, cookie)
        appConfig.componentRegistry.auditLogger.log(Login(credentials))
        createAuthCookieResponse(credentials)
      }
      case (None, Some(cookie)) => {
        logger.warn("No user OID found. Cookie: " + cookie)
        response.redirect(request.getContextPath + "/no-applications.html")
      }
      case _ => redirectToShibbolethLogin(response)
    }
  }

  private def findHetuFromParams = {
    headerOption("Hetu") match {
      case Some(hetu) => Some(hetu)
      case None if appConfig.usesFakeAuthentication => paramOption("hetu")
      case _ => {
        logger.warn("No 'Hetu' header found.")
        None
      }
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
    response.redirect(redirectUri)
  }

  private def redirectUri: String = {
    request.getContextPath + paramOption("redirect").getOrElse("/index.html")
  }
}