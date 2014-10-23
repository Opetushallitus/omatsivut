package fi.vm.sade.omatsivut.servlet

import javax.servlet.http.HttpServletRequest

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.security.AuthCookieParsing
import org.apache.commons.io.IOUtils
import org.scalatra.ScalatraFilter

class UserFilter(val appConfig: AppConfig) extends ScalatraFilter with AuthCookieParsing {
  before() {
    val httpRequest: HttpServletRequest = request
    shibbolethCookieInRequest(httpRequest) match {
      case Some(_) if (personOidOption(httpRequest).isEmpty) =>
        response.setContentType("text/html;charset=UTF-8")
        IOUtils.copy(request.getServletContext.getResourceAsStream("/no-applications.html"), response.getOutputStream)
        response.getOutputStream.flush()
        halt()
      case _ =>
    }
  }

}
