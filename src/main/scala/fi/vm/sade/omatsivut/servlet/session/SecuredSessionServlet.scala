package fi.vm.sade.omatsivut.servlet.session

import java.util.UUID

import fi.vm.sade.omatsivut.security.{AuthenticationInfoService, CookieNames, OppijaNumero, SessionService}
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra.{BadRequest, Cookie, InternalServerError}

trait SecuredSessionServletContainer {
  class SecuredSessionServlet(val authenticationInfoService: AuthenticationInfoService, val sessionService: SessionService)
    extends OmatSivutServletBase with CookieNames with OmatsivutPaths with Logging {

    private def initializeSessionAndRedirect(personOid: String): Unit = {
      val newSession = sessionService.storeSession(OppijaNumero(personOid))
      newSession match {
        case Right((sessionId, _)) =>
          response.addCookie(Cookie(sessionCookieName, sessionId.value.toString))
          response.addCookie(Cookie(oppijaNumeroCookieName, personOid))
          response.redirect(redirectUri)
        case Left(e) => halt(500, "unable to create session, exception = " + e)
      }
    }

    private def clientAddress = " [" + request.getRemoteAddr + "]"

    get("/") {
      logger.info("Initsession exxxxecution")
      val hetu = request.header("hetu")
      logger.info("Initsession hetu parameter = " + hetu)
      hetu match {
        case Some(hetu) => {
          authenticationInfoService.getHenkiloOID(hetu) match {
            case Some(henkiloOid) => initializeSessionAndRedirect(henkiloOid)
            case _ => initializeSessionAndRedirect("")
          }
        }
        case None =>
          BadRequest(reason = "No hetu found in request from shibboleth" + clientAddress)
      }
    }

    private def redirectUri: String = {
      val link = omatsivutPath(request.getContextPath) + paramOption("redirect").getOrElse("/index.html")
      logger.info("Link to forward to, after a session is established: " + link)
      link
    }
  }
}
