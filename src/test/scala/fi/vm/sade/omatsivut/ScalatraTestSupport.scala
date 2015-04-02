package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.config.{AppConfig, ComponentRegistry}
import fi.vm.sade.omatsivut.security.fake.FakeAuthentication
import fi.vm.sade.utils.tcp.PortChecker
import org.scalatra.test.HttpComponentsClient
import org.specs2.mutable.Specification
import org.specs2.specification.{Fragments, Step}

trait ScalatraTestSupport extends Specification with HttpComponentsClient {
  lazy val appConfig = AppConfigSetup.create
  lazy val componentRegistry = new ComponentRegistry(appConfig)

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

  override def map(fs: => Fragments) = Step(SharedJetty.start) ^ super.map(fs)
}

object SharedJetty {
  private lazy val jettyLauncher = new JettyLauncher(Some("it"))

  def start {
    jettyLauncher.start
  }
}

object AppConfigSetup {
  lazy val create = new AppConfig.IT
}

case class PersonOid(oid: String)
