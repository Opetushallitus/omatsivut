package fi.vm.sade.omatsivut.servlet.session

import java.util.UUID

import fi.vm.sade.hakemuseditori.auditlog.Audit
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.auditlog.Logout
import fi.vm.sade.omatsivut.security.{AttributeNames, CookieHelper, SessionId, SessionService}
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import javax.servlet.http.HttpServletRequest
import org.scalatra.servlet.RichResponse

trait LogoutServletContainer {

  class LogoutServlet(implicit val sessionService: SessionService) extends OmatSivutServletBase with AttributeNames {
    get("/*") {
      sessionService.deleteSession(cookies.get(sessionCookieName).map(UUID.fromString).map(SessionId))
      clearCookie(sessionCookieName)
      redirectToShibbolethLogout(request, response)
    }

    def sendLogOut(): Unit = {
      Audit.oppija.log(Logout(request))
      redirectToShibbolethLogout(request, response)
    }

    def clearCookie(name: String): Unit = {
      CookieHelper.getCookie(request, name).foreach(cookie => {
        cookie.setValue("")
        cookie.setPath("/")
        cookie.setMaxAge(0)
        response.addCookie(cookie)
      })
    }

    def redirectToShibbolethLogout(request: HttpServletRequest, response: RichResponse): Unit = {
      val koskiParameter = request.getParameter("koski")
      val returnUrl = if (koskiParameter != null && koskiParameter == "true")
        "/oma-opintopolku"
      else
        "/koski/user/logout"
      redirect(OphUrlProperties.url("shibboleth.logout", returnUrl))(request, response.res)
    }
  }
}

