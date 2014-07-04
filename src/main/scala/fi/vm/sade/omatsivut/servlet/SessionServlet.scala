package fi.vm.sade.omatsivut.servlet

import org.scalatra.{CookieOptions, Cookie}
import fi.vm.sade.omatsivut.security.{AuthenticationInfoService, CookieCredentials, AuthCookieParsing, AuthenticationCipher}
import scala.collection.JavaConverters._

class SessionServlet(implicit val authService: AuthenticationInfoService) extends AuthCookieCreating {
  get("/initsession") {
    request.getHeaderNames.asScala.toList.map(h => logger.info(h + ": " + request.getHeader(h)))
    createAuthCookieResponse(headerOption("Hetu"), redirectUri = paramOption("redirect").getOrElse("/index.html"))
  }

  get("/logout") {
    tellBrowserToDeleteAuthCookie(request, response)
    response.redirect("/Shibboleth.sso/Logout")
  }
}