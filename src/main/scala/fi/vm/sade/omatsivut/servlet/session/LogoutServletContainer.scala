package fi.vm.sade.omatsivut.servlet.session

import java.util.UUID

import javax.servlet.http.{Cookie, HttpServletRequest}
import fi.vm.sade.hakemuseditori.auditlog.Audit
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.auditlog.Logout
import fi.vm.sade.omatsivut.security.{AttributeNames, SessionId, SessionService}
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
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
      val cookies: Array[Cookie] = request.getCookies
      if (cookies == null) {
        return
      }
      cookies
        .filter(_.getName == name)
        .foreach(cookie => {
          cookie.setValue("")
          cookie.setPath("/")
          cookie.setMaxAge(0);
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

