package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.security.{AuthCookieParsing, AuthenticationCipher, AuthenticationInfoService, CookieCredentials}
import org.scalatra.{Cookie, CookieOptions}

trait AuthCookieCreating extends OmatSivutServletBase with AuthCookieParsing with Logging {
  def createAuthCookieResponse(hetuOption: Option[String], cookieOptions: CookieOptions = CookieOptions(secure = true, path = "/", maxAge = 1799), redirectUri: String)(implicit appConfig: AppConfig) {
    fetchOid(hetuOption, AuthenticationInfoService.apply) match {
      case Some(oid) =>
        val encryptedCredentials = AuthenticationCipher().encrypt(CookieCredentials(oid).toString)
        response.addCookie(Cookie("auth", encryptedCredentials)(cookieOptions))
        logger.info("Redirecting to " + redirectUri)
        response.redirect(request.getContextPath + redirectUri)
      case _ =>
        logger.warn("OID not found for hetu: " + headerOption("hetu"))
        response.redirect(ssoContextPath + "/Shibboleth.sso/LoginFI") //TODO Localization
    }
  }

  def fetchOid(hetuOption: Option[String], authService: AuthenticationInfoService) = {
    for {
      hetu <- hetuOption
      oid <- authService.getHenkiloOID(hetu)
    } yield oid
  }

  def ssoContextPath: String

  protected def headerOption(name: String): Option[String] = {
    Option(request.getHeader(name))
  }

  protected def paramOption(name: String): Option[String] = {
    try {
      Option(params(name))
    } catch {
      case e: Exception => None
    }
  }
}
