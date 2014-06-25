package fi.vm.sade.omatsivut

import org.scalatra.{CookieOptions, Cookie}
import scala.collection.JavaConversions._

class SessionServlet extends OmatsivutStack {

  get("/initsession") {
    for (x <- request.getHeaderNames) {
      logger.info(x)
    }
    createResponse(() => headerOption("Hetu"), redirectUri = paramOption("redirect").getOrElse("/index.html"))
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
        logger.info("Redirecting to " + redirectUri)
        response.redirect(redirectUri)
      case _ =>
        logger.warn("OID not found for hetu: " + headerOption("hetu"))
        response.setStatus(401)
    }
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
