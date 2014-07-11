package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.security.{AuthenticationCipher, CookieCredentials}
import fi.vm.sade.omatsivut.servlet.OmatSivutSwagger
import org.scalatra.test.specs2.MutableScalatraSpec

trait ScalatraTestSupport extends MutableScalatraSpec {
  implicit val appConfig = AppConfig.fromSystemProperty
  implicit val swagger = new OmatSivutSwagger

  def authGet[A](uri: String, oid : String)(f: => A): A = {
    get(uri, headers = authHeaders(oid))(f)
  }

  def authPost[A](uri: String, oid: String, body: Array[Byte])(f: => A): A = {
    post(uri, body, headers = authHeaders(oid))(f)
  }

  def authHeaders[A](oid: String): Map[String, String] = {
    Map("Cookie" -> ("auth=" + AuthenticationCipher().encrypt(CookieCredentials(oid).toString)))
  }
}
