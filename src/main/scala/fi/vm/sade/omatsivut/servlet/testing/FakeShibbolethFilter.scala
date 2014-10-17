package fi.vm.sade.omatsivut.servlet.testing

import javax.servlet.http.HttpServletRequestWrapper

import fi.vm.sade.omatsivut.security.FakeAuthentication
import fi.vm.sade.omatsivut.util.Logging
import org.scalatra.ScalatraFilter

class FakeShibbolethFilter extends ScalatraFilter with Logging {
  before() {
    val requestWrapper = new HttpServletRequestWrapper(request) {
      override def getHeader(name: String): String = {
        if(name == "oid") {
          FakeAuthentication.fakeOidInRequest(request).get
        } else {
          super.getHeader(name)
        }
      }
    }
    _request.value_=(requestWrapper)
  }
}
