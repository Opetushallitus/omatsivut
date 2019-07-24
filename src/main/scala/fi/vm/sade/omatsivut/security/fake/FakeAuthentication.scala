package fi.vm.sade.omatsivut.security.fake

import fi.vm.sade.omatsivut.security.{AttributeNames, SessionId}

object FakeAuthentication extends AttributeNames {

  def authHeaders[A](oid: String, sessionId: SessionId): Map[String, String] = {
    Map("Cookie" -> (sessionCookieName + "=" + sessionId.value.toString))
  }
}
