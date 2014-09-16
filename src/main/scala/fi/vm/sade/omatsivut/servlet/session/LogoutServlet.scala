package fi.vm.sade.omatsivut.servlet.session

import javax.servlet.http.HttpServletRequest

import fi.vm.sade.omatsivut.config.AppConfig
import AppConfig.AppConfig
import fi.vm.sade.omatsivut.auditlog.{AuditLoggerComponent, AuditLogger, Logout}
import fi.vm.sade.omatsivut.security.{AuthenticationCipher, AuthCookieParsing, CookieCredentials}
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import org.scalatra.servlet.RichResponse

trait LogoutServletContainer {
  this: AuditLoggerComponent =>

  val auditLogger: AuditLogger

  class LogoutServlet(implicit val appConfig: AppConfig) extends OmatSivutServletBase with AuthCookieParsing {
    get("/*") {
      parseCredentials(request, new AuthenticationCipher(appConfig.settings.aesKey, appConfig.settings.hmacKey)) match {
        case Some(credentials) => sendLogOut(credentials)
        case _ => redirectToShibbolethLogout(request, response)
      }
    }

    def sendLogOut(credentials: CookieCredentials) {
      auditLogger.log(Logout(credentials))
      tellBrowserToDeleteAuthCookie(request, response)
      redirectToShibbolethLogout(request, response)
    }

    def redirectToShibbolethLogout(request: HttpServletRequest, response: RichResponse): Unit = {
      val returnUrl = request.getContextPath + "/session/reset"
      response.redirect(appConfig.authContext.ssoContextPath + "/Shibboleth.sso/Logout?return=" + returnUrl)
    }
  }
}

