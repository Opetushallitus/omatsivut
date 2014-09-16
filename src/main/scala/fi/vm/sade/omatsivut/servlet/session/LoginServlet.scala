package fi.vm.sade.omatsivut.servlet.session

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase

class LoginServlet(implicit val appConfig: AppConfig) extends OmatSivutServletBase with ShibbolethPaths {
  get("/*") {
    redirectToShibbolethLogin(response, appConfig.authContext.ssoContextPath)
  }
}
