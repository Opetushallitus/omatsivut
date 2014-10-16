package fi.vm.sade.omatsivut.servlet

import javax.servlet._
import javax.servlet.http.HttpServletRequest

import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.security.AuthCookieParsing
import fi.vm.sade.omatsivut.util.Timer
import org.apache.commons.io.IOUtils

class UserFilter extends Filter with AuthCookieParsing {
  implicit lazy val appConfig = AppConfig.fromSystemProperty
  override def init(filterConfig: FilterConfig) {}

  override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    Timer.timed(blockname = "doFilter") {
      val httpRequest: HttpServletRequest = request.asInstanceOf[HttpServletRequest]
      shibbolethCookieInRequest(httpRequest) match {
        case Some(_) if (personOidOption(httpRequest).isEmpty) =>
          response.setContentType("text/html;charset=UTF-8")
          IOUtils.copy(request.getServletContext.getResourceAsStream("/no-applications.html"), response.getOutputStream)
          response.getOutputStream.flush()
        case _ =>
          chain.doFilter(request, response)
      }
    }
  }

  override def destroy() {}
}
