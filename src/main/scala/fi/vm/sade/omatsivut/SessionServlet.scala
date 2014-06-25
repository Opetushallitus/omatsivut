package fi.vm.sade.omatsivut

import org.scalatra.{CookieOptions, Cookie}

class SessionServlet extends OmatsivutStack {

  get("/initsession") {
    createResponse(() => headerOption("Hetu"))
  }

  def createResponse(hetuOption: () => Option[String], cookieOptions: CookieOptions = CookieOptions(secure = true, path = "/"), redirectUri: String = "/index.html") {
    val oid = for {
      hetu <- hetuOption()
      oid <- AuthenticationInfoService.getHenkiloOID(hetu)
    } yield oid
    oid match {
      case Some(str) =>
        val encryptedOid = AuthenticationCipher.encrypt(str)
        response.addCookie(Cookie("auth", encryptedOid)(cookieOptions))
        val uri: String = redirectContextPath + redirectUri
        logger.info("Redirecting to " + uri)
        response.redirect(uri)
      case _ =>
        logger.warn("OID not found for hetu: " + headerOption("hetu"))
        response.setStatus(401)
    }
  }

  def redirectContextPath = {
    val cp = request.getContextPath
    if(cp.isEmpty) cp else cp.substring(1)
  }

  get("/fakesession") {
    createResponse(() => paramOption("hetu"), CookieOptions(secure = false, path = "/"), paramOption("redirect").getOrElse("/index.html"))
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
