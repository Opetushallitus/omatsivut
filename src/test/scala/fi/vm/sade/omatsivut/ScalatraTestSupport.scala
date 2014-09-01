package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.security.{ShibbolethCookie, AuthenticationCipher, CookieCredentials}
import fi.vm.sade.omatsivut.servlet.OmatSivutSwagger
import org.scalatra.test.specs2.MutableScalatraSpec
import org.specs2.specification.{Step, Fragments}

trait ScalatraTestSupport extends MutableScalatraSpec {
  implicit lazy val appConfig = AppConfigSetup.create
  implicit val swagger = new OmatSivutSwagger

  def authGet[A](uri: String, oid : String)(f: => A): A = {
    get(uri, headers = authHeaders(oid))(f)
  }

  def authPost[A](uri: String, oid: String, body: Array[Byte])(f: => A): A = {
    post(uri, body, headers = authHeaders(oid))(f)
  }

  def authPut[A](uri: String, oid: String, body: Array[Byte])(f: => A): A = {
    put(uri, body, headers = authHeaders(oid))(f)
  }

  def authHeaders[A](oid: String): Map[String, String] = {
    val shibbolethCookie: ShibbolethCookie = ShibbolethCookie("_shibsession_test", "test")
    Map("Cookie" -> ("auth=" + AuthenticationCipher().encrypt(CookieCredentials(oid, shibbolethCookie).toString) + "; " + shibbolethCookie))
  }

  override def map(fs: => Fragments) = Step(appConfig.start) ^ super.map(fs)
}

object AppConfigSetup {
  lazy val create = AppConfig.fromSystemProperty
}
