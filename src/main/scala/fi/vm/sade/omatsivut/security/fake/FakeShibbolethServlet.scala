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
class FakeShibbolethServlet(val appConfig: AppConfig) extends OmatSivutServletBase with CookieNames {

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

}
