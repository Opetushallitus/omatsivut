package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import javax.servlet.http.HttpServletRequest
import org.scalatra.servlet.RichResponse

trait ShibbolethLogout {

  def redirectToShibbolethLogout(request: HttpServletRequest, response: RichResponse)(implicit appConfig: AppConfig): Unit = {
    val returnUrl = request.getContextPath + "/session/reset"
    response.redirect(appConfig.authContext.ssoContextPath + "/Shibboleth.sso/Logout?return=" + returnUrl)
  }
}
