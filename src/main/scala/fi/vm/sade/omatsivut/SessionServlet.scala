package fi.vm.sade.omatsivut

import org.scalatra.{CookieOptions, Cookie}

class SessionServlet extends OmatsivutStack {

  get("/initsession") {
    createResponse(() => headerOption("Hetu"))
  }

  def createResponse(hetuOption: () => Option[String]) {
    val oid = for {
      hetu <- hetuOption()
      oid <- AuthenticationInfoService.getHenkiloOID(hetu)
    } yield oid
    oid match {
      case Some(str) =>
        val encryptedOid = AuthenticationCipher.encrypt(str)
        response.addCookie(Cookie("auth", encryptedOid)(CookieOptions(secure = true)))
        response.redirect("/index.html")
      case _ =>
        logger.warn("OID not found for hetu: " + headerOption("hetu"))
        response.setStatus(503)
    }
  }

  get("/fakesession") {
    createResponse(() => paramOption("hetu"))
  }

  private def headerOption(name: String): Option[String] = {
    Option(request.getHeader(name))
  }

  private def paramOption(name: String): Option[String] = {
    try {
      Option(params(name))
    } catch {
      case e: Exception => None
    }
  }
}
