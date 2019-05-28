package fi.vm.sade.omatsivut.security.fake

import fi.vm.sade.omatsivut.security.AttributeNames
import javax.servlet.http.{Cookie, HttpServletRequest}

object FakeAuthentication extends AttributeNames {

  def authHeaders[A](oid: String, sessionId: String): Map[String, String] = {
    Map("Cookie" -> (sessionCookieName + "=" + sessionId))
  }
}
