package fi.vm.sade.hakemuseditori.auditlog

import java.net.InetAddress
import fi.vm.sade.auditlog.User

import javax.servlet.http.HttpServletRequest
import fi.vm.sade.omatsivut.util.Logging
import org.ietf.jgss.Oid
import fi.vm.sade.omatsivut.security.SessionInfoRetriever.getSessionId
import org.apache.commons.lang3.StringUtils

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

  private val HARMLESS_URLS: Seq[String] = parseHarmlessUrlsFromSystemProperty
  private val SKIP_MISSING_HEADER_LOGGING: Boolean = "true" == (System.getProperty("fi.vm.sade.javautils.http.HttpServletRequestUtils.SKIP_MISSING_HEADER_LOGGING"))

  if (SKIP_MISSING_HEADER_LOGGING) logger.warn("Skipping missing real IPs logging. This should not be used in production.")

  private def parseHarmlessUrlsFromSystemProperty: Seq[String] = {
    val property: String = System.getProperty("fi.vm.sade.javautils.http.HttpServletRequestUtils.HARMLESS_URLS")
    if (StringUtils.isEmpty(property)) {
      return Seq()
    }
    property.split(",")
  }

  private def getRemoteAddress(httpServletRequest: HttpServletRequest): String = getRemoteAddress(httpServletRequest.getHeader("X-Real-IP"), httpServletRequest.getHeader("X-Forwarded-For"), httpServletRequest.getRemoteAddr, httpServletRequest.getRequestURI)

  private def getRemoteAddress(xRealIp: String, xForwardedFor: String, remoteAddr: String, requestURI: String): String = {
    if (StringUtils.isNotEmpty(xRealIp)) return xRealIp
    if (StringUtils.isNotEmpty(xForwardedFor)) {
      if (xForwardedFor.contains(",")) logger.error("Could not find X-Real-IP header, but X-Forwarded-For contains multiple values: {}, " + "this can cause problems", xForwardedFor)
      return xForwardedFor
    }
    if (!SKIP_MISSING_HEADER_LOGGING && !HARMLESS_URLS.contains(requestURI)) logger.warn(String.format("X-Real-IP or X-Forwarded-For was not set. Are we not running behind a load balancer? Request URI is '%s'", requestURI))
    remoteAddr
  }
}
