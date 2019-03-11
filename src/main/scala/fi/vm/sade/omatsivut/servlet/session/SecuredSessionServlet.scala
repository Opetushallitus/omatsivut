package fi.vm.sade.omatsivut.servlet.session

import java.util.UUID

import fi.vm.sade.omatsivut.security.{AuthenticationInfoService, CookieNames, OppijaNumero, SessionService}
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import org.scalatra.{BadRequest, Cookie, InternalServerError}

import scala.xml.{Elem, SAXParseException, XML}

trait SecuredSessionServletContainer {
  class SecuredSessionServlet(val authenticationInfoService: AuthenticationInfoService, val sessionService: SessionService)
    extends OmatSivutServletBase with CookieNames {

    private def getHetuFromSaml(msg: Elem): Option[String] = {
      (for {
        item <- msg \\ "NameID" if (item \ "@Format").text == "urn:oid:1.2.246.21"
      } yield item.text.trim) match {
        case List(hetu) if !hetu.isEmpty => Some(hetu.trim)
        case _ => None
      }
    }

    private def getMsgId(msg: Elem): Option[String] = {
      (for {
        idAttr <- msg \\ "AttributeQuery" \ "@ID"
      } yield idAttr.text.trim) match {
        case List(id) if !id.isEmpty => Some(id.trim)
        case _ => None
      }
    }

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

    post("/") {
      try {
        val r = request.body
        val msg = XML.loadString(request.body)
        getHetuFromSaml(msg) match {
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
      catch {
        case e: SAXParseException => {
          logger.warn("invalid soap message " + clientAddress)
          halt(400, reason = "Invalid SOAP (SAML) message")
        }
        case e: Exception => {
          logger.error("internal error " + e.toString, e)
          InternalServerError(e.toString)
        }
      }

    }

    private def redirectUri: String = {
      request.getContextPath + paramOption("redirect").getOrElse("/index.html")
    }
  }
}
