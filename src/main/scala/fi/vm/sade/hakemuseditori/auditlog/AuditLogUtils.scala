package fi.vm.sade.hakemuseditori.auditlog

import java.net.InetAddress

import fi.vm.sade.auditlog.User
import javax.servlet.http.HttpServletRequest
import fi.vm.sade.utils.slf4j.Logging
import org.ietf.jgss.Oid

import scala.util.{Failure, Success, Try}
import fi.vm.sade.javautils.http.HttpServletRequestUtils.getRemoteAddress
import fi.vm.sade.omatsivut.security.SessionInfoRetriever.getSessionId

class AuditLogUtils extends Logging {
  val USER_AGENT = "User-Agent"

  protected def getOid(oid: String): Oid = if (oid != null) new Oid(oid) else null

  protected def getAddress(request: HttpServletRequest): InetAddress = {
    InetAddress.getByName(getRemoteAddress(request))
  }

  protected def getUserAgent(request: HttpServletRequest): String = {
    request.getHeader(USER_AGENT)
  }

  protected def getUser(oid: String, request: HttpServletRequest): User = {
    new User(getOid(oid), getAddress(request), getSessionId(request).getOrElse("(no session cookie)"), getUserAgent(request))
  }
}
