package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.security._
import org.scalatra.servlet.RichResponse
import org.scalatra.{Cookie, CookieOptions}

trait AuthCookieCreating extends AuthCookieParsing with Logging {
  def createAuthCookieResponse(credentials: CookieCredentials,
                               response: RichResponse,
                               redirectUri: String,
                               cookieOptions: CookieOptions = CookieOptions(secure = true, path = "/", maxAge = 1799))(implicit appConfig: AppConfig) {
    val encryptedCredentials = AuthenticationCipher().encrypt(credentials.toString)
    response.addCookie(Cookie("auth", encryptedCredentials)(cookieOptions))
    logger.info("Redirecting to " + redirectUri)
    response.redirect(redirectUri)
  }

  def createAuthCookieCredentials(hetu: Option[String], shibbolethCookie: Option[ShibbolethCookie], authenticationInfoService: AuthenticationInfoService): Option[CookieCredentials] = {
    checkCredentials(hetu, shibbolethCookie, authenticationInfoService) match {
      case Some((oid, cookie)) => Some(CookieCredentials(oid, cookie))
      case _ => {
        logger.warn("Person oid not found for hetu: " + hetu)
        None
      }
    }
  }

  private def checkCredentials(hetuOption: Option[String], shibbolethCookie: Option[ShibbolethCookie], authService: AuthenticationInfoService) = {
    for {
      hetu <- hetuOption
      cookie <- shibbolethCookie
      oid <- authService.getHenkiloOID(hetu)
    } yield (oid, cookie)
  }
}
