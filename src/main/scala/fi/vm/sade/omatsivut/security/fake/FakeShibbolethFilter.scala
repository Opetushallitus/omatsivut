package fi.vm.sade.omatsivut.security.fake

import javax.servlet.http.{HttpServletRequest, HttpServletRequestWrapper}
import fi.vm.sade.omatsivut.security.CookieHelper
import fi.vm.sade.omatsivut.util.Logging
import org.scalatra.ScalatraFilter

class FakeShibbolethFilter extends ScalatraFilter with Logging {
  before() {
    val requestWrapper = new HttpServletRequestWrapper(request) {
      override def getHeader(name: String): String = {
        if(List("oid", "entitlement").contains(name)) {
          cookieValue(FakeAuthentication.fakeCookiePrefix + name, request).getOrElse(super.getHeader(name))
        } else {
          super.getHeader(name)
        }
      }
    }
    _request.value_=(requestWrapper)
  }

  def cookieValue(name: String, req: HttpServletRequest): Option[String] = {
    CookieHelper.reqCookie(req, c => c.getName == name && c.getValue != "").map(_.getValue)
  }
}
