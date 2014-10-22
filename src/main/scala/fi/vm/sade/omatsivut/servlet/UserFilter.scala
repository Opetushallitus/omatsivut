package fi.vm.sade.omatsivut.servlet

import javax.servlet._
import javax.servlet.http.HttpServletRequest

import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.security.{CookieCredentials, AuthCookieParsing}
import fi.vm.sade.omatsivut.util.Timer
import org.apache.commons.io.IOUtils

class UserFilter extends Filter with AuthCookieParsing {
  implicit lazy val appConfig = AppConfig.fromSystemProperty
  override def init(filterConfig: FilterConfig) {}

  override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    credentialsOption(request.asInstanceOf[HttpServletRequest]) match {
      case Some(credentials) if (credentials.oidMissing) =>
        IOUtils.copy(request.getServletContext.getResourceAsStream("/no-applications.html"), response.getOutputStream)
        response.getOutputStream.flush()
      case _ =>
        chain.doFilter(request, response)
    }
  }

  override def destroy() {}
}
