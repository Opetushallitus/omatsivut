package fi.vm.sade.omatsivut

import org.scalatra.{CookieOptions, Cookie}
import fi.vm.sade.omatsivut.security.{AuthenticationInfoService, CookieCredentials, AuthCookieParsing, AuthenticationCipher}
import scala.collection.JavaConverters._

class SessionServlet extends OmatsivutStack with AuthCookieParsing {
  get("/initsession") {
    request.getHeaderNames.asScala.toList.map(h => logger.info(h + ": " + request.getHeader(h)))
    createResponse(() => headerOption("Hetu"), redirectUri = paramOption("redirect").getOrElse("/index.html"))
  }

  get("/logout") {
    tellBrowserToDeleteAuthCookie(request, response)
    response.redirect("/Shibboleth.sso/Logout")
  }

  def createResponse(hetuOption: () => Option[String], cookieOptions: CookieOptions = CookieOptions(secure = true, path = "/", maxAge = 1799), redirectUri: String) {
    fetchOid(hetuOption) match {
      case Some(oid) =>
        val encryptedCredentials = AuthenticationCipher.encrypt(CookieCredentials(oid).toString)
        response.addCookie(Cookie("auth", encryptedCredentials)(cookieOptions))
        logger.info("Redirecting to " + redirectUri)
        response.redirect(request.getContextPath + redirectUri)
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
