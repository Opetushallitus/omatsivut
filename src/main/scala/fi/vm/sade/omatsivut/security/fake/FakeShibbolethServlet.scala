package fi.vm.sade.omatsivut.security.fake

import java.util.concurrent.TimeUnit

import fi.vm.sade.omatsivut.OphUrlProperties
import javax.servlet.http.{HttpServletRequest, HttpServletResponse, Cookie => HttpCookie}
import fi.vm.sade.omatsivut.config.AppConfig.{AppConfig, StubbedExternalDeps}
import fi.vm.sade.omatsivut.security.CookieHelper.reqCookie
import fi.vm.sade.omatsivut.security.{CookieHelper, CookieNames}
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase
import org.http4s._
import org.http4s.client.blaze
import org.http4s.Status.Found
import org.http4s.util.CaseInsensitiveString
import org.scalatra.Ok
import scalaz.concurrent.Task

import scala.concurrent.duration.Duration

/**
 * Simulates the actual Shibboleth server with AA (Attribute Authority) that provides the SSN->personOid mapping.
 *
 * Provides /fakesession?hetu=123456-7890 url for starting a session with given SSN.
 *
 * @param appConfig
 */
class FakeShibbolethServlet(val appConfig: AppConfig) extends OmatSivutServletBase with FakeSAMLMessages with CookieNames {

  case class SessionCookies(sessionId: Option[String], oppijaNumero: Option[String])

  get("/fakesession") {
    paramOption("hetu") match {
      case Some(hetu) =>
        doSamlPostWithHetu(hetu, request.getContextPath + "/initsession") match {
          case Right(SessionCookies(sessionId, oppijaNumero)) => {
            cookies.update(sessionCookieName, sessionId.getOrElse(""))
            cookies.update(oppijaNumeroCookieName, oppijaNumero.getOrElse(""))
          }
          case Left(e) =>
            halt(400, "Could not create session via /initsession (" + e + ")")
        }
      case _ =>
        halt(400, "Can't fake session without ssn")
    }
  }

  get("/Logout") {
    paramOption("return") match {
      case Some(url) => response.redirect(url)
      case _ => redirectToFakeLogin
    }
  }

  get("/Login*") {
    redirectToFakeLogin
  }

  private def redirectToFakeLogin {
    response.redirect(request.getContextPath + "/fakeVetumaLogin.html")
  }

  private def doSamlPostWithHetu(hetu: String, url: String): Either[Throwable, SessionCookies] = {
    val timeout = Duration(30, TimeUnit.SECONDS)
    val bodyWithHetu: Array[Byte] = createSamlBodyWithHetu(hetu)

    def uriFromString(url: String): Uri = {
      Uri.fromString(url).toOption.get
    }

    val uri = uriFromString(OphUrlProperties.url("url-oppija") + url)

    val samlRequest = Request(
      method = Method.POST,
      uri = uri
    ).withBody(bodyWithHetu)

    val httpClient = blaze.defaultClient

    def getCookie(resp: Response, name: String): Option[String] = {
      resp.headers.
        filter(_.name.equals(CaseInsensitiveString("Set-Cookie"))).
        map(x => CookieHelper.cookieExtractValue(x.value, name)).
        find(_.isDefined).
        get
    }

    try {
      var sessionCookie: Option[String] = None
      var oppijaNumeroCookie: Option[String] = None
      httpClient.fetch(samlRequest) {
        case Found(resp) =>
          sessionCookie = getCookie(resp, sessionCookieName)
          oppijaNumeroCookie = getCookie(resp, oppijaNumeroCookieName)
          Task.now(true)
        case r => r.as[String].map { body =>
          throw new RuntimeException("Unexpected response from initsession " + body)
        }
      }.runFor(timeout)
      Right(SessionCookies(sessionCookie, oppijaNumeroCookie))
    } catch {
      case e: Throwable =>
        Left(new RuntimeException("Failed initsession POST request: " + e.toString))
    }
  }

}
