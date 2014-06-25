package fi.vm.sade.omatsivut

import org.scalatra.{CookieOptions, Cookie}
import scala.collection.JavaConverters._

class SessionServlet extends OmatsivutStack {

  get("/initsession") {
    request.getHeaderNames.asScala.toList.map(h => logger.info(h + ": " + request.getHeader(h)))
    createResponse(() => headerOption("Hetu"), redirectUri = paramOption("redirect").getOrElse("/index.html"))
  }

  def createResponse(hetuOption: () => Option[String], cookieOptions: CookieOptions = CookieOptions(secure = true, path = "/", maxAge = 1799), redirectUri: String = "/index.html") {
    fetchOid(hetuOption) match {
      case Some(oid) =>
        val encryptedCredentials = AuthenticationCipher.encrypt(CookieCredentials(oid).toString)
        response.addCookie(Cookie("auth", encryptedCredentials)(cookieOptions))
        logger.info("Redirecting to " + redirectUri)
        response.redirect(redirectUri)
      case _ =>
        logger.warn("OID not found for hetu: " + headerOption("hetu"))
        response.setStatus(401)
    }
  }

  def fetchOid(hetuOption: () => Option[String]) = {
    for {
      hetu <- hetuOption()
      oid <- AuthenticationInfoService.getHenkiloOID(hetu)
    } yield oid
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
