package fi.vm.sade.omatsivut

import java.net.HttpCookie

import fi.vm.sade.hakemuseditori.hakemus.HakemusSpringContext
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.security.fake.FakeAuthentication
import org.scalatra.test.{ClientResponse, HttpComponentsClient}
import org.specs2.mutable.Specification

trait ScalatraTestSupport extends Specification with HttpComponentsClient with OmatsivutDbTools with ITSetup {

  protected lazy val springContext: HakemusSpringContext = SharedAppConfig.componentRegistry.springContext

  var lastSessionId: String = ""

  step {
    SharedJetty.start
    springContext
  }

  def baseUrl = "http://localhost:" + AppConfig.embeddedJettyPortChooser.chosenPort + "/omatsivut"

  def authGet[A](uri: String)(f: => A)(implicit personOid: PersonOid): A = {
    val sessionId = createTestSession()
    lastSessionId = sessionId
    get(uri, headers = FakeAuthentication.authHeaders(personOid.oid, sessionId))(f)
  }

  def authPost[A](uri: String, body: Array[Byte])(f: => A)(implicit personOid: PersonOid): A = {
    val sessionId = createTestSession()
    lastSessionId = sessionId
    post(uri, body, headers = FakeAuthentication.authHeaders(personOid.oid, sessionId))(f)
  }

  def authPut[A](uri: String, body: Array[Byte])(f: => A)(implicit personOid: PersonOid): A = {
    val sessionId = createTestSession()
    lastSessionId = sessionId
    put(uri, body, headers = FakeAuthentication.authHeaders(personOid.oid, sessionId))(f)
  }

  def postJSON[T](path: String, body: String, headers: Map[String, String] = Map.empty)(block: => T): T = {
    post(path, body.getBytes("UTF-8"), Map("Content-type" -> "application/json") ++ headers)(block)
  }
}

trait ScalatraTestCookiesSupport {
  /**
    * Helper to create a headers map with the cookies specified. Merge with another map for more headers.
    *
    * This allows only basic cookies, no expiry or domain set.
    *
    * @param cookies key-value pairs
    * @return a map suitable for passing to a get() or post() Scalatra test method
    */
  def cookieHeaderWith(cookies: Map[String, String]): Map[String, String] = {
    val asHttpCookies = cookies.map { case (k, v) => new HttpCookie(k, v) }
    val headerValue = asHttpCookies.mkString("; ")
    Map("Cookie" -> headerValue)
  }

  /**
    * Syntatically nicer function for cookie header creation:
    *
    * cookieHeaderWith("testcookie" -> "what")
    *
    * instead of
    * cookieHeaderWith(Map("testcookie" -> "what"))
    *
    * @param cookies
    * @return
    */
  def cookieHeaderWith(cookies: (String, String)*): Map[String, String] = {
    cookieHeaderWith(cookies.toMap)
  }

  def cookieExtractValue(cookieString: String, cookieName: String): Option[String] = {
    val cookieComponents = cookieString.split(Array('=', ';'))
    if (cookieComponents.length > 2) {
      if (cookieComponents(0) == cookieName) Some(cookieComponents(1)) else None
    }
    else None
  }

  def cookieGetValue(response: ClientResponse, cookieName: String): Option[String] = {
    response.headers("Set-Cookie").map(cookieExtractValue(_, cookieName)).find(v => v.isDefined && v.get.length > 0).getOrElse(None)
  }
}

case class PersonOid(oid: String)
