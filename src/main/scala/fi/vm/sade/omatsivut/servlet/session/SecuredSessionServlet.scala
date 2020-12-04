package fi.vm.sade.omatsivut.servlet.session

import fi.vm.sade.hakemuseditori.auditlog.Audit
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.auditlog.Login
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.security._
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import fi.vm.sade.utils.cas.{CasClient, CasLogout}
import fi.vm.sade.utils.cas.CasClient.{OppijaAttributes, ServiceTicket}
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra.{BadRequest, Cookie, CookieOptions}
import scalaz.concurrent.Task

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
          val attrs: Either[Throwable, OppijaAttributes] = casOppijaClient.validateServiceTicket(initsessionPath(request.getContextPath()))(ticket, casOppijaClient.decodeOppijaAttributes).handleWith {
            case NonFatal(t) => Task.fail(new AuthenticationFailedException(s"Failed to validate service ticket $ticket", t))
          }.attemptRunFor(10000).toEither
          logger.debug(s"attrs response: $attrs")
          attrs match {
            case Right(attrs) => {
              logger.info(s"User logging in: $attrs")
              if (isUsingValtuudet(attrs)) {
                logger.info(s"User ${attrs.getOrElse("impersonatorDisplayName", "NOT_FOUND")} is using valtuudet; Will not init session and should redirect to ${valtuudetRedirectUri}")
                redirect(valtuudetRedirectUri)
              } else {
                val hetu = attrs("nationalIdentificationNumber")
                val personOid = attrs.getOrElse("personOid", "")
                val displayName = attrs.getOrElse("displayName", "")
                initializeSessionAndRedirect(ticket, hetu, personOid, displayName)
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

    post("/") {
      params.get("logoutRequest") match {
        case Some(x) => {
          CasLogout.parseTicketFromLogoutRequest(x) match {
            case Some(ticket: ServiceTicket) => sessionService.deleteSessionByServiceTicket(ticket)
            case None => new RuntimeException(s"Failed to parse CAS logout request $request")
          }
        }
        case None => new IllegalArgumentException("Not 'logoutRequest' parameter given")
      }
    }

    private def initializeSessionAndRedirect(ticket: ServiceTicket, hetu: String, personOid: String, displayName: String): Unit = {
      val newSession = sessionService.storeSession(ticket, Hetu(hetu), OppijaNumero(personOid), displayName)
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

    private def valtuudetRedirectUri: String = {
      "https://" + OphUrlProperties.url("host.oppija") + "/oma-opintopolku/"
    }

    private def isUsingValtuudet(attributes: OppijaAttributes): Boolean = {
      (attributes.getOrElse("impersonatorNationalIdentificationNumber", "").nonEmpty
        || attributes.getOrElse("impersonatorDisplayName", "").nonEmpty)
    }
  }

}
