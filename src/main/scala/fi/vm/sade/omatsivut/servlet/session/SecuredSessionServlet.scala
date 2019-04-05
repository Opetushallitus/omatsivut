package fi.vm.sade.omatsivut.servlet.session

import java.nio.charset.Charset
import java.util.UUID

import fi.vm.sade.omatsivut.security._
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import fi.vm.sade.utils.slf4j.Logging
import org.joda.time.LocalDate
import org.scalatra.{BadRequest, Cookie, InternalServerError}

trait SecuredSessionServletContainer {
  class SecuredSessionServlet(val authenticationInfoService: AuthenticationInfoService, val sessionService: SessionService)
    extends OmatSivutServletBase with AttributeNames with OmatsivutPaths with Logging {

    get("/") {
      logger.debug("initsession request received")
      val hetu = request.header("hetu")
      val firstName: Option[String] = Option(request.getHeader("firstname"))
      val lastName: Option[String] = Option(request.getHeader("sn"))
      val displayName = parseDisplayName(firstName, lastName)

      hetu match {
        case Some(hetu) => {
          authenticationInfoService.getHenkiloOID(hetu) match {
            case Some(henkiloOid) => initializeSessionAndRedirect(hetu, henkiloOid, displayName)
            case _ => initializeSessionAndRedirect(hetu, "", displayName)
          }
        }
        case None =>
          BadRequest(reason = "No hetu found in request from shibboleth" + clientAddress)
      }
    }

    private def initializeSessionAndRedirect(hetu: String, personOid: String, displayName: String): Unit = {
      val newSession = sessionService.storeSession(Hetu(hetu), OppijaNumero(personOid), displayName)
      newSession match {
        case Right((sessionId, _)) =>
          response.addCookie(Cookie(sessionCookieName, sessionId.value.toString))
          response.redirect(redirectUri)
        case Left(e) =>
          logger.error("Unable to create session. (" + e + ")")
          halt(500, "unable to create session, exception = " + e)
      }
    }

    private def clientAddress = " [" + request.getRemoteAddr + "]"

    private def redirectUri: String = {
      val link = omatsivutPath(request.getContextPath) + paramOption("redirect").getOrElse("/index.html")
      logger.info("Link to forward to, after a session is established: " + link)
      link
    }

    private def parseDisplayName(firstName: Option[String], lastName: Option[String]): String = {
      // Dekoodataan etunimet ja sukunimi manuaalisesti, koska shibboleth välittää ASCII-enkoodatut request headerit UTF-8 -merkistössä

      val iso88591 = Charset.forName("ISO-8859-1")
      val utf8 = Charset.forName("UTF-8")
      val builder = new StringBuilder
      List(firstName, lastName).flatten.
        map(n => new String(n.getBytes(iso88591), utf8)).
        mkString(" ")
    }

    private val sensitiveHeaders = List("security", "hetu")
    private val headersWhiteList = List("FirstName", "cn", "givenName", "hetu", "oid", "security", "sn")
    private def headers: String = {
      request.headers.toList.collect { case (name, value) if headersWhiteList.contains(name) =>
        if (sensitiveHeaders.contains(name)) {
          (name, "*********")
        } else {
          (name, value)
        }
      }.sortBy(_._1).mkString("\n")
    }
  }
}
