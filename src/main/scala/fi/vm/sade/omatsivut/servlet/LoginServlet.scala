package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig

class LoginServlet(implicit val appConfig: AppConfig) extends OmatSivutServletBase with ShibbolethPaths {
  get("/*") {
    redirectToShibbolethLogin(response)
  }
}
