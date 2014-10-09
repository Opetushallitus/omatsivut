package fi.vm.sade.omatsivut.servlet.session

import fi.vm.sade.omatsivut.config.AppConfig
import AppConfig.AppConfig
import fi.vm.sade.omatsivut.auditlog.{AuditLoggerComponent, AuditLogger, Login}
import fi.vm.sade.omatsivut.security._
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import org.apache.commons.io.IOUtils
import org.scalatra.Cookie

import scala.collection.JavaConverters._

trait SecuredSessionServletContainer {
  this: AuditLoggerComponent with AuthenticationInfoComponent =>

  val authenticationInfoService: AuthenticationInfoService
  val auditLogger: AuditLogger

  class SecuredSessionServlet(val appConfig: AppConfig) extends OmatSivutServletBase with AuthCookieParsing with ShibbolethPaths {
    get("/initsession") {
      checkCredentials match {
        case (oid, Some(cookie)) => {
          if (!oid.isDefined) {
            logger.warn("No user OID found. Cookie: " + cookie)
          }
          val credentials: CookieCredentials = CookieCredentials(cookie, oid)
          auditLogger.log(Login(credentials))
          createAuthCookieResponse(credentials)
        }
        case _ => redirectToShibbolethLogin(response, appConfig.authContext.ssoContextPath)
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
        oid <- authenticationInfoService.getHenkiloOID(hetu)
      } yield oid
      (oid, shibbolethCookieInRequest(request))
    }

    private def createAuthCookieResponse(credentials: CookieCredentials) {
      val encryptedCredentials = new AuthenticationCipher(appConfig.settings.aesKey, appConfig.settings.hmacKey).encrypt(credentials.toString)
      response.addCookie(Cookie("auth", encryptedCredentials)(appConfig.authContext.cookieOptions))
      response.redirect(redirectUri)
    }

    private def redirectUri: String = {
      request.getContextPath + paramOption("redirect").getOrElse("/index.html")
    }
  }
}

