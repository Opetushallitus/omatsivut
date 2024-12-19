package fi.vm.sade.omatsivut.servlet.session

import fi.vm.sade.hakemuseditori.auditlog.Audit
import fi.vm.sade.javautils.nio.cas.{CasClient, CasLogout}
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.auditlog.Login
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.security._
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import fi.vm.sade.omatsivut.util.{Logging, OptionConverter}
import org.scalatra.{BadRequest, Cookie, CookieOptions}
import scala.concurrent.ExecutionContext.Implicits.global

import java.util.concurrent.CompletableFuture
import java.util.{HashMap => JHashMap}
import scala.compat.java8.FutureConverters._
import scala.collection.JavaConverters._
import scala.concurrent._
import scala.util.{Failure, Success}

trait SecuredSessionServletContainer {

  class SecuredSessionServlet(val appConfig: AppConfig,
                              val authenticationInfoService: AuthenticationInfoService,
                              implicit val sessionService: SessionService,
                              val sessionTimeout: Option[Int] = None,
                              val casOppijaClient: CasClient)
    extends OmatSivutServletBase with AttributeNames with OmatsivutPaths with Logging {
    get("/") {
      logger.debug("initsession CAS request received")

      val ticket: Option[String] = Option(request.getParameter("ticket"))
      ticket match {
        case None => BadRequest("No ticket found from CAS request" + clientAddress);
        case Some(ticket) => {
          callValidateServiceTicketWithOppijaAttributes("service1", "ticket1").onComplete {
            case Failure(exception) =>
              new AuthenticationFailedException(s"Failed to validate service ticket $ticket", exception)
              // TODO toteuta retryt, vanha: .attemptRunFor(10000).toEither
            case Success(attrs) =>
              logger.debug(s"attrs response: $attrs")
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
          }
        }
      }

    post("/") {
      params.get("logoutRequest") match {
        case Some(x) => {
          val casLogout = new CasLogout()
          val ticket = OptionConverter.javaOptionalToScalaOption(casLogout.parseTicketFromLogoutRequest(x))
          ticket match {
            case Some(ticket: String) => sessionService.deleteSessionByServiceTicket(ticket)
            case None => new RuntimeException(s"Failed to parse CAS logout request $request")
          }
        }
        case None => new IllegalArgumentException("Not 'logoutRequest' parameter given")
      }
    }

    private def callValidateServiceTicketWithOppijaAttributes(service: String, ticket: String): Future[Map[String, String]] = {
      val javaFuture: CompletableFuture[JHashMap[String, String]] =
        casOppijaClient.validateServiceTicketWithOppijaAttributes(initsessionPath(request.getContextPath()), ticket)
      val scalaFuture: Future[JHashMap[String, String]] = javaFuture.toScala
      scalaFuture.map(_.asScala.toMap)
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

    private def valtuudetRedirectUri: String = {
      "https://" + OphUrlProperties.url("host.oppija") + "/oma-opintopolku/"
    }

    private def isUsingValtuudet(attributes: Map[String, String]): Boolean = {
      (attributes.getOrElse("impersonatorNationalIdentificationNumber", "").nonEmpty
        || attributes.getOrElse("impersonatorDisplayName", "").nonEmpty)
    }
  }

}
