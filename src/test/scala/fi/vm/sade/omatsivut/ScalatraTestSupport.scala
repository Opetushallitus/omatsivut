package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.security.{AuthenticationCipher, CookieCredentials, ShibbolethCookie}
import fi.vm.sade.omatsivut.servlet.OmatSivutSwagger
import org.scalatra.test.specs2.MutableScalatraSpec
import org.specs2.specification.{Fragments, Step}

trait ScalatraTestSupport extends MutableScalatraSpec {
  implicit val swagger = new OmatSivutSwagger
  lazy val appConfig = AppConfigSetup.create

  def authGet[A](uri: String)(f: => A)(implicit personOid: PersonOid): A = {
    get(uri, headers = authHeaders(personOid.oid))(f)
  }

  def authPost[A](uri: String, oid: String, body: Array[Byte])(f: => A)(implicit personOid: PersonOid): A = {
    post(uri, body, headers = authHeaders(personOid.oid))(f)
  }

  def authPut[A](uri: String, body: Array[Byte])(f: => A)(implicit personOid: PersonOid): A = {
    put(uri, body, headers = authHeaders(personOid.oid))(f)
  }

  def authHeaders[A](oid: String): Map[String, String] = {
    val shibbolethCookie: ShibbolethCookie = ShibbolethCookie("_shibsession_test", "test")
    Map("Cookie" -> ("auth=" + new AuthenticationCipher(appConfig.settings.aesKey, appConfig.settings.hmacKey).encrypt(CookieCredentials(oid, shibbolethCookie).toString) + "; " + shibbolethCookie))
  }

  override def map(fs: => Fragments) = Step(appConfig.componentRegistry.start) ^ super.map(fs)
}

object AppConfigSetup {
  lazy val create = AppConfig.fromSystemProperty
}

case class PersonOid(oid: String)