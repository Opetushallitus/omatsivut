package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.security.{AuthCookieParsing, AuthenticationCipher, AuthenticationInfoService, CookieCredentials}
import org.scalatra.{Cookie, CookieOptions}

trait AuthCookieCreating extends OmatSivutServletBase with AuthCookieParsing with Logging {
  def createAuthCookieResponse(credentials: CookieCredentials, cookieOptions: CookieOptions = CookieOptions(secure = true, path = "/", maxAge = 1799), redirectUri: String)(implicit appConfig: AppConfig) {
    val encryptedCredentials = AuthenticationCipher().encrypt(credentials.toString)
    response.addCookie(Cookie("auth", encryptedCredentials)(cookieOptions))
    logger.info("Redirecting to " + redirectUri)
    response.redirect(request.getContextPath + redirectUri)
  }

  def createAuthCookieCredentials(hetuOption: Option[String], shibbolethCookie: String, authenticationInfoService: AuthenticationInfoService): Option[CookieCredentials] = {
    fetchOid(hetuOption, authenticationInfoService) match {
      case Some(oid) => Some(CookieCredentials(oid, shibbolethCookie))
      case _ => {
        logger.warn("Person oid not found for hetu: " + hetuOption)
        None
      }
    }
  }

  def fetchOid(hetuOption: Option[String], authService: AuthenticationInfoService) = {
    for {
      hetu <- hetuOption
      oid <- authService.getHenkiloOID(hetu)
    } yield oid
  }

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
