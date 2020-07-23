package fi.vm.sade.omatsivut.servlet.session

import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

import fi.vm.sade.hakemuseditori.auditlog.Audit
import fi.vm.sade.omatsivut.auditlog.Login
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.security._
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import fi.vm.sade.utils.cas.CasClient.Username
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra.{BadRequest, Cookie, CookieOptions}
import fi.vm.sade.utils.cas.{CasAuthenticatingClient, CasClient, CasParams}
import org.http4s.client.blaze
import scalaz.concurrent.Task

import scala.concurrent.duration.Duration
import scala.util.control.NonFatal

trait SecuredSessionServletContainer {
  class SecuredSessionServlet(val appConfig: AppConfig,
                              val authenticationInfoService: AuthenticationInfoService,
                              implicit val sessionService: SessionService,
                              val sessionTimeout: Option[Int] = None,
                              val casOppijaClient: CasClient)
    extends OmatSivutServletBase with AttributeNames with OmatsivutPaths with Logging {

    get("/") {
      logger.debug("initsession CAS request received")

      val ticket: Option[CasClient.ServiceTicket] = Option(request.getParameter("ticket"))

      ticket match {
        case None => BadRequest("No ticket found from CAS request" + clientAddress);
        case Some(ticket) => {
          val hetu: Either[Throwable, String] = casOppijaClient.validateServiceTicket(initsessionPath())(ticket).handleWith {
            case NonFatal(t) => Task.fail(new AuthenticationFailedException(s"Failed to validate service ticket $ticket", t))
          }.attemptRunFor(10000).toEither

          hetu match {
            case Right(hetu) => {
              authenticationInfoService.getOnrHenkilo(hetu) match {
                case Some(onrHenkilo) => initializeSessionAndRedirect(onrHenkilo.hetu,
                  onrHenkilo.oidHenkilo,
                  onrHenkilo.etunimet + " " + onrHenkilo.sukunimi)
                case None => initializeSessionAndRedirect(hetu, "", "")  // TODO: virhe?
              }
            }
            case Left(t) => {
              logger.warn("Unable to process CAS Oppija login request, hetu cannot be resolved from ticket", t)
              BadRequest(t.getMessage)
            }
          }
        }
      }
    }

    private def initializeSessionAndRedirect(hetu: String, personOid: String, displayName: String): Unit = {
      val newSession = sessionService.storeSession(Hetu(hetu), OppijaNumero(personOid), displayName)
      newSession match {
        case Right((sessionId, _)) =>
          val cookieOptions = CookieOptions(domain = "", secure = isHttps, path = "/", maxAge = sessionTimeout.getOrElse(3600), httpOnly = true)
          val sessionCookie: Cookie = Cookie(sessionCookieName, sessionId.value.toString)(cookieOptions)
          logger.debug(s"Created new session with id $sessionId , adding session cookie $sessionCookie with options $cookieOptions to response")
          response.addCookie(sessionCookie)
          Audit.oppija.log(Login(request))
          redirect(redirectUri)
        case Left(e) =>
          logger.error("Unable to create session. (" + e + ")", e)
          halt(500, "unable to create session, exception = " + e)
      }
    }

    private def clientAddress = " [" + request.getRemoteAddr + "]"

    private def redirectUri: String = {
      val link = omatsivutPath(request.getContextPath) + paramOption("redirect").getOrElse("/index.html")
      logger.debug("Link to forward to, after a session is established: " + link)
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
