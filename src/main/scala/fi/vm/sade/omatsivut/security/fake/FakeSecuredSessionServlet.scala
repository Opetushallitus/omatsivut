package fi.vm.sade.omatsivut.security.fake

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fi.vm.sade.hakemuseditori.auditlog.Audit
import fi.vm.sade.omatsivut.auditlog.Login
import fi.vm.sade.omatsivut.cas.CasClient.OppijaAttributes
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.security.{AttributeNames, AuthenticationFailedException, AuthenticationInfoService, Hetu, OppijaNumero, SessionService}
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import fi.vm.sade.omatsivut.servlet.session.OmatsivutPaths
import fi.vm.sade.omatsivut.util.Logging
import org.scalatra.{BadRequest, Cookie, CookieOptions}

import java.util
import java.util.concurrent.CompletableFuture
import scala.collection.JavaConverters.mapAsScalaMapConverter
import scala.compat.java8.FutureConverters.CompletionStageOps
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

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
      val ticket: Option[String] = Option(request.getParameter("ticket"))
      ticket match {
        case None => BadRequest("No ticket found from CAS request" + clientAddress);
        case Some(ticket) => {
          logger.debug("GOT TICKET FROM CAS")
          val result: IO[Either[Throwable, OppijaAttributes]] = fakeCasOppijaClient.validateServiceTicketWithOppijaAttributes(request.getContextPath())(hetu).attempt
          result.unsafeRunSync() match {
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

    private def initializeSessionAndRedirect(ticket: String, hetu: String, personOid: String, displayName: String): Unit = {
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
