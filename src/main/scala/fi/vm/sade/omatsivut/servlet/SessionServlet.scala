package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig

import scala.collection.JavaConverters._

class SessionServlet(implicit val appConfig: AppConfig) extends AuthCookieCreating {
  get("/initsession") {
    request.getHeaderNames.asScala.toList.map(h => logger.info(h + ": " + request.getHeader(h)))
    createAuthCookieResponse(headerOption("Hetu"), redirectUri = paramOption("redirect").getOrElse("/index.html"))
  }

  get("/logout") {
    tellBrowserToDeleteAuthCookie(request, response)
    response.redirect(ssoContextPath + "/Shibboleth.sso/Logout?type=Local")
  }

  def ssoContextPath: String = if (appConfig.isTest) "/omatsivut" else "/"
}