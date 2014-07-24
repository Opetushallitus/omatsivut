package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.security.AuthenticationInfoService

import scala.collection.JavaConverters._

class SessionServlet(implicit val appConfig: AppConfig) extends OmatSivutServletBase with AuthCookieCreating {
  get("/initsession") {
    request.getHeaderNames.asScala.toList.map(h => logger.info(h + ": " + request.getHeader(h)))
    createAuthCookieCredentials(headerOption("Hetu"), "placeholder", AuthenticationInfoService.apply) match {
      case Some(credentials) => createAuthCookieResponse(credentials, response, redirectUri)
      case _ => response.redirect(ssoContextPath + "/Shibboleth.sso/LoginFI") //TODO Localization
    }
  }

  def redirectUri: String = {
    request.getContextPath + paramOption("redirect").getOrElse("/index.html")
  }

  get("/logout") {
    tellBrowserToDeleteAuthCookie(request, response)
    response.redirect(ssoContextPath + "/Shibboleth.sso/Logout?type=Local")
  }

  def ssoContextPath: String = if (appConfig.isTest) "/omatsivut" else "/"
}