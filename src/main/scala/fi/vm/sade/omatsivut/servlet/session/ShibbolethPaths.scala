package fi.vm.sade.omatsivut.servlet.session

import javax.servlet.http.HttpServletRequest
import fi.vm.sade.omatsivut.config.AppConfig
import AppConfig.AppConfig
import fi.vm.sade.omatsivut.domain.Language
import org.scalatra.servlet.RichResponse

trait ShibbolethPaths {

  def redirectToShibbolethLogin(response: RichResponse)(implicit appConfig: AppConfig, lang: Language.Language) {
    response.redirect(appConfig.authContext.ssoContextPath + "/Shibboleth.sso/Login" + lang.toString().toUpperCase())
  }
  
  def redirectToShibbolethLogout(request: HttpServletRequest, response: RichResponse)(implicit appConfig: AppConfig): Unit = {
    val returnUrl = request.getContextPath + "/session/reset"
    response.redirect(appConfig.authContext.ssoContextPath + "/Shibboleth.sso/Logout?return=" + returnUrl)
  }
}
