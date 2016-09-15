package fi.vm.sade.omatsivut.servlet.session

import fi.vm.sade.omatsivut.security.AuthenticationContext
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase

class LoginServlet(val authenticationContext: AuthenticationContext) extends OmatSivutServletBase with ShibbolethPaths {
  get("/*") {
    redirectToShibbolethLogin(response, authenticationContext.ssoContextPath)
  }
}
