package fi.vm.sade.omatsivut.security.fake

import fi.vm.sade.hakemuseditori.auditlog.Audit
import fi.vm.sade.omatsivut.auditlog.Login
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.security.{AttributeNames, AuthenticationFailedException, AuthenticationInfoService, Hetu, OppijaNumero, SessionService}
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import fi.vm.sade.omatsivut.servlet.session.OmatsivutPaths
import fi.vm.sade.utils.cas.CasClient
import fi.vm.sade.utils.cas.CasClient.{OppijaAttributes, ServiceTicket}
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra.{BadRequest, Cookie, CookieOptions}
import scalaz.concurrent.Task

import scala.util.control.NonFatal

trait FakeSecuredSessionServletContainer {
  class FakeSecuredSessionServlet(val appConfig: AppConfig,
                                  val authenticationInfoService: AuthenticationInfoService,
                                  implicit val sessionService: SessionService,
                                  val sessionTimeout: Option[Int] = None,
                                  val fakeCasOppijaClient: FakeCasClient)
    extends OmatSivutServletBase with AttributeNames with OmatsivutPaths with Logging {

    get("/") {
      logger.debug("initsession CAS request received")
      val hetu: String = request.getHeader("hetu")
      val ticket: Option[CasClient.ServiceTicket] = Option(request.getParameter("ticket"))
      ticket match {
        case None => BadRequest("No ticket found from CAS request" + clientAddress);
        case Some(ticket) => {
          logger.debug("GOT TICKET FROM CAS")
          val attrs: Either[Throwable, OppijaAttributes] = fakeCasOppijaClient.validateServiceTicket(initsessionPath(request.getContextPath))(ticket, fakeCasOppijaClient.decodeOppijaAttributes(hetu)).handleWith {
            case NonFatal(t) => Task.fail(new AuthenticationFailedException(s"Failed to validate service ticket $ticket", t))
          }.attemptRunFor(10000).toEither
          attrs match {
            case Right(attrs) => {
              val hetu = attrs("nationalIdentificationNumber")
              val personOid = attrs.getOrElse("personOid", "")
              val displayName = attrs.getOrElse("displayName", "")
              logger.debug("ATTRIBUTES:")
              initializeSessionAndRedirect(ticket, hetu, personOid, displayName)
            }
            case Left(t) => {
              logger.warn("Unable to process CAS Oppija login request, hetu cannot be resolved from ticket", t)
              BadRequest(t.getMessage)
            }
          }
        }
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
  }
}
