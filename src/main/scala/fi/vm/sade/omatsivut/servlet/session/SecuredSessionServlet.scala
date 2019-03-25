package fi.vm.sade.omatsivut.servlet.session

import java.util.UUID

import fi.vm.sade.omatsivut.security.{AuthenticationInfoService, CookieNames, OppijaNumero, SessionService}
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import org.scalatra.{BadRequest, Cookie, InternalServerError}

import scala.xml.{Elem, SAXParseException, XML}

trait SecuredSessionServletContainer {
  class SecuredSessionServlet(val authenticationInfoService: AuthenticationInfoService, val sessionService: SessionService)
    extends OmatSivutServletBase with CookieNames with OmatsivutPaths {

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
      val hetu = request.header("hetu")
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
      omatsivutPath(request.getContextPath) + paramOption("redirect").getOrElse("/index.html")
    }
  }
}
