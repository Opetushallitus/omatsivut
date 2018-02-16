package fi.vm.sade.hakemuseditori.auditlog

import java.net.InetAddress
import javax.servlet.http.HttpServletRequest

import fi.vm.sade.utils.slf4j.Logging
import org.ietf.jgss.Oid

import scala.util.{Failure, Success, Try}
import fi.vm.sade.javautils.http.HttpServletRequestUtils.getRemoteAddress

class AuditLogUtils extends Logging {
  val USER_AGENT = "User-Agent"

  protected def getOid(oid: String): Option[Oid] = {
    Try(new Oid(oid)) match {
      case Success(v) => Some(v)
      case Failure(e) => {
        logger.error("Error creating Oid-object out of {}", oid)
        None
      }
    }
  }

  protected def getAddress(request: HttpServletRequest): InetAddress = {
    Try(InetAddress.getByName(getRemoteAddress(request))) match {
      case Success(v) => v
      case Failure(e) => {
        logger.error("Error creating InetAddress: ", e)
        InetAddress.getLocalHost
      }
    }
  }

  protected def getSession(request: HttpServletRequest): String = {
    request.getSession.getId
  }

  protected def getUserAgent(request: HttpServletRequest): String = {
    request.getHeader(USER_AGENT)
  }
}
