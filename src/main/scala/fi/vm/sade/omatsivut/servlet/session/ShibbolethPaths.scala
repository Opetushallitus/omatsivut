package fi.vm.sade.omatsivut.servlet.session

import javax.servlet.http.HttpServletRequest

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import org.scalatra.servlet.RichResponse

trait ShibbolethPaths {

  def redirectToShibbolethLogin(response: RichResponse)(implicit appConfig: AppConfig) {
    response.redirect(appConfig.authContext.ssoContextPath + "/Shibboleth.sso/LoginFI") //TODO Localization 
  }
  
  def redirectToShibbolethLogout(request: HttpServletRequest, response: RichResponse)(implicit appConfig: AppConfig): Unit = {
    val returnUrl = request.getContextPath + "/session/reset"
    response.redirect(appConfig.authContext.ssoContextPath + "/Shibboleth.sso/Logout?return=" + returnUrl)
  }
}
