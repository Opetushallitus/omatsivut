package fi.vm.sade.omatsivut.servlet.session

import java.util.UUID

import javax.servlet.http.HttpServletRequest
import fi.vm.sade.hakemuseditori.auditlog.Audit
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.auditlog.Logout
import fi.vm.sade.omatsivut.security.{CookieNames, SessionId, SessionService}
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import org.scalatra.servlet.RichResponse

trait LogoutServletContainer {

  class LogoutServlet(val sessionService: SessionService) extends OmatSivutServletBase with CookieNames {
    get("/*") {
      sessionService.deleteSession(cookies.get(sessionCookieName).map(UUID.fromString).map(SessionId))
      clearCookie(sessionCookieName)
      clearCookie(oppijaNumeroCookieName)
      redirectToShibbolethLogout(request, response)
    }

    def sendLogOut {
      Audit.oppija.log(Logout(request))
      redirectToShibbolethLogout(request, response)
    }

    def clearCookie(name: String) = {
      request.getCookies
        .filter(cookie => cookie.getName == name)
        .foreach(cookie => {
          cookie.setValue("")
          cookie.setPath("/")
          cookie.setMaxAge(0);
          response.addCookie(cookie)
        })
    }

    def redirectToShibbolethLogout(request: HttpServletRequest, response: RichResponse): Unit = {
      val returnUrl = "/oma-opintopolku" // check authentication context for test specific context-path ?
      response.redirect(OphUrlProperties.url("shibboleth.logout", returnUrl))
    }
  }
}

