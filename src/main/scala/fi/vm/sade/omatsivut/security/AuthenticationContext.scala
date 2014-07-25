package fi.vm.sade.omatsivut.security

import org.scalatra.CookieOptions

trait AuthenticationContext {
  def cookieOptions: CookieOptions
  def ssoContextPath: String
}

class ProductionAuthenticationContext extends AuthenticationContext {
  def cookieOptions = CookieOptions(secure = true, path = "/", maxAge = 1799)
  def ssoContextPath = ""
}

class TestAuthenticationContext extends AuthenticationContext {
   def cookieOptions = CookieOptions(path = "/")
   def ssoContextPath: String = "/omatsivut"
}