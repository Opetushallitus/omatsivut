package fi.vm.sade.omatsivut.servlet.session

import java.util.UUID

import fi.vm.sade.hakemuseditori.auditlog.Audit
import fi.vm.sade.omatsivut.auditlog.Logout
import fi.vm.sade.omatsivut.security.{AttributeNames, CookieHelper, SessionId, SessionService}
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import javax.servlet.http.HttpServletRequest
import org.scalatra.servlet.RichResponse

trait LogoutServletContainer extends OmatsivutPaths {

  class LogoutServlet(implicit val sessionService: SessionService) extends OmatSivutServletBase with AttributeNames {
    get("/*") {
      sessionService.deleteSession(cookies.get(sessionCookieName).map(UUID.fromString).map(SessionId))
      clearCookie(sessionCookieName)
      redirectToCASOppijaLogout(request, response)
    }

    post("/") {
      params.get("logoutRequest")
        .map(_ => {
          sessionService.deleteSession(cookies.get(sessionCookieName).map(UUID.fromString).map(SessionId))
          clearCookie(sessionCookieName)
          redirectToCASOppijaLogout(request, response)
        })
        .orElse(throw new IllegalArgumentException("Not 'logoutRequest' parameter given"))//) toRight(new IllegalArgumentException("Not 'logoutRequest' parameter given"))
        //.right.flatMap(request => CasLogout.parseTicketFromLogoutRequest(request).toRight(new RuntimeException(s"Failed to parse CAS logout request $request")))
        //.right.flatMap(ticket => sessionService.deleteSession(ServiceTicket(ticket))) match {
       // .flatMap(_ => sessionService.deleteSession(cookies.get(sessionCookieName).map(UUID.fromString).map(SessionId)))
//      ) match {
//        case Right(_) => NoContent()
//        case Left(t) => throw t
//      }
    }

    def sendLogOut(): Unit = {
      Audit.oppija.log(Logout(request))
      redirectToCASOppijaLogout(request, response)
    }

    def clearCookie(name: String): Unit = {
      CookieHelper.getCookie(request, name).foreach(cookie => {
        cookie.setValue("")
        cookie.setPath("/")
        cookie.setMaxAge(0)
        response.addCookie(cookie)
      })
    }

    def redirectToCASOppijaLogout(request: HttpServletRequest, response: RichResponse): Unit = {
      val koskiParameter = request.getParameter("koski")
      val returnPath = if (koskiParameter != null && koskiParameter == "true")
        "/oma-opintopolku"
      else
        "/koski/user/logout"
      val logoutRedirectUrl = logoutPath(returnPath)
      logger.debug(s"Redirecting to $logoutRedirectUrl for logout")
      redirect(logoutRedirectUrl)(request, response.res)
    }
  }
}

