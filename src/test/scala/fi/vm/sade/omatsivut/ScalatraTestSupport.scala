package fi.vm.sade.omatsivut

import org.scalatra.test.specs2.MutableScalatraSpec
import fi.vm.sade.omatsivut.security.{CookieCredentials, AuthenticationCipher}

trait ScalatraTestSupport extends MutableScalatraSpec {

  def authGet[A](uri: String, oid : String)(f: => A): A = {
    get(uri, headers = authHeaders(oid))(f)
  }

  def authPost[A](uri: String, oid: String, body: Array[Byte])(f: => A): A = {
    post(uri, body, headers = authHeaders(oid))(f)
  }

  def authHeaders[A](oid: String): Map[String, String] = {
    Map("Cookie" -> ("auth=" + AuthenticationCipher.encrypt(CookieCredentials(oid).toString)))
  }
}
