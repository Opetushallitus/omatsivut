package fi.vm.sade.omatsivut

import fi.vm.sade.hakemuseditori.hakemus.HakemusSpringContext
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.security.fake.FakeAuthentication
import fi.vm.sade.omatsivut.security.{CookieHelper, SessionId}
import org.scalatra.test.{ClientResponse, HttpComponentsClient}
import org.specs2.mutable.Specification

trait ScalatraTestSupport extends Specification with HttpComponentsClient with OmatsivutDbTools with ITSetup {

  protected lazy val springContext: HakemusSpringContext = SharedAppConfig.componentRegistry.springContext

  step {
    SharedJetty.start
    springContext
  }

  def baseUrl: String = "http://localhost:" + AppConfig.embeddedJettyPortChooser.chosenPort + "/omatsivut"

  def authGet[A](uri: String)(f: => A)(implicit personOid: PersonOid): A = {
    authGetAndReturnSession[A](uri)(_ => f)
  }

  def authGetAndReturnSession[A](uri: String)(f: SessionId => A)(implicit personOid: PersonOid): A = {
    val sessionId = createTestSession()
    get(uri, headers = FakeAuthentication.authHeaders(personOid.oid, sessionId))(f(sessionId))
  }

  def authPost[A](uri: String, body: Array[Byte])(f: => A)(implicit personOid: PersonOid): A = {
    val sessionId = createTestSession()
    post(uri, body, headers = FakeAuthentication.authHeaders(personOid.oid, sessionId))(f)
  }

  def authPut[A](uri: String, body: Array[Byte])(f: => A)(implicit personOid: PersonOid): A = {
    val sessionId = createTestSession()
    put(uri, body, headers = FakeAuthentication.authHeaders(personOid.oid, sessionId))(f)
  }

  def postJSON[T](path: String, body: String, headers: Map[String, String] = Map.empty)(block: => T): T = {
    post(path, body.getBytes("UTF-8"), Map("Content-type" -> "application/json") ++ headers)(block)
  }
}

trait ScalatraTestCookiesSupport {
  def cookieGetValue(response: ClientResponse, cookieName: String): Option[String] = {
    response.headers("Set-Cookie").
      map(CookieHelper.cookieExtractValue(_, cookieName)).
      find(v => v.isDefined && v.get.length > 0).
      flatten
  }
}

case class PersonOid(oid: String)
