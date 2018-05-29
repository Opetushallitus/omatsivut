package fi.vm.sade.omatsivut.servlet.session

import javax.servlet.http.HttpServletRequest

import fi.vm.sade.hakemuseditori.auditlog.Audit
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.auditlog.Logout
import fi.vm.sade.omatsivut.security.AuthenticationContext
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import org.scalatra.servlet.RichResponse

trait LogoutServletContainer {

  class LogoutServlet(val authenticationContext: AuthenticationContext) extends OmatSivutServletBase {
    get("/*") {
      redirectToShibbolethLogout(request, response)
    }

    def sendLogOut {
      Audit.oppija.log(Logout(request))
      redirectToShibbolethLogout(request, response)
    }

    def redirectToShibbolethLogout(request: HttpServletRequest, response: RichResponse): Unit = {
      val returnUrl = request.getContextPath + "/"
      response.redirect(authenticationContext.ssoContextPath + OphUrlProperties.url("shibboleth.logout", returnUrl))
    }
  }
}

