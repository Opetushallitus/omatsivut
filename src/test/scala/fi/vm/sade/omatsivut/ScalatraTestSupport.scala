package fi.vm.sade.omatsivut

import fi.vm.sade.hakemuseditori.hakemus.HakemusSpringContext
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.security.fake.FakeAuthentication
import org.scalatra.test.HttpComponentsClient
import org.specs2.mutable.Specification

trait ScalatraTestSupport extends Specification with HttpComponentsClient {

  protected lazy val springContext: HakemusSpringContext = SharedAppConfig.componentRegistry.springContext

  step {
    SharedJetty.start
    springContext
  }

  def baseUrl = "http://localhost:" + AppConfig.embeddedJettyPortChooser.chosenPort + "/omatsivut"

  def authGet[A](uri: String)(f: => A)(implicit personOid: PersonOid): A = {
    get(uri, headers = FakeAuthentication.authHeaders(personOid.oid))(f)
  }

  def authPost[A](uri: String, body: Array[Byte])(f: => A)(implicit personOid: PersonOid): A = {
    post(uri, body, headers = FakeAuthentication.authHeaders(personOid.oid))(f)
  }

  def authPut[A](uri: String, body: Array[Byte])(f: => A)(implicit personOid: PersonOid): A = {
    put(uri, body, headers = FakeAuthentication.authHeaders(personOid.oid))(f)
  }

  def postJSON[T](path: String, body: String, headers: Map[String, String] = Map.empty)(block: => T): T = {
    post(path, body.getBytes("UTF-8"), Map("Content-type" -> "application/json") ++ headers)(block)
  }
}


case class PersonOid(oid: String)
