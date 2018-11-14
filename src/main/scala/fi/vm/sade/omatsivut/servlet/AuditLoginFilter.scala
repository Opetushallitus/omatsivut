package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori.auditlog.Audit
import fi.vm.sade.omatsivut.auditlog.Login
import fi.vm.sade.omatsivut.security.AuthenticationInfoParser._
import org.apache.http.HttpHeaders
import org.scalatra.ScalatraFilter

class AuditLoginFilter(vetumaUrl: String) extends ScalatraFilter {

  before() {
    for {
      referer <- Option(request.getHeader(HttpHeaders.REFERER))
      vetuma <- Some(referer).filter(_.startsWith(vetumaUrl))
      info <- Option(getAuthenticationInfo(request))
      personOid <- info.oppijaNumero
    } Audit.oppija.log(Login(request))
  }

}
