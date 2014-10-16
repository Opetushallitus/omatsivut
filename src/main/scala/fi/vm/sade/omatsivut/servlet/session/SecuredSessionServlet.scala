package fi.vm.sade.omatsivut.servlet.session

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

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
      personOidOption(request.asInstanceOf[HttpServletRequest]) match {
        case (Some(oid)) => {
          auditLogger.log(Login(AuthInfo()))
          response.redirect(redirectUri)
        }
        case _ => redirectToShibbolethLogin(response, appConfig.authContext.ssoContextPath)
      }
    }

    private def redirectUri: String = {
      request.getContextPath + paramOption("redirect").getOrElse("/index.html")
    }
  }
}