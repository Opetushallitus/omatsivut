package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.auditlog.{ApplicationType, Logger, Audit => AuditClass}
import fi.vm.sade.utils.slf4j.Logging

object Audit {
  def oppija = new OppijaAuditLogger
  def virkailija = new VirkailijaAuditLogger
  def api = new ApiAuditLogger
}

class OppijaAuditLogger(applicationType: ApplicationType) extends AuditLogger(applicationType) {
  def this() = this(ApplicationType.OPPIJA)
}

class VirkailijaAuditLogger(applicationType: ApplicationType) extends AuditLogger(applicationType) {
  def this() = this(ApplicationType.VIRKAILIJA)
}

class ApiAuditLogger(applicationType: ApplicationType) extends AuditLogger(applicationType) {
  def this() = this(ApplicationType.BACKEND)
}

class AuditLogger(val omatSivutLogger: OmatSivutLogger, val serviceName: String, val applicationType: ApplicationType) extends Logging {
  def this(applicationType: ApplicationType) = this(new OmatSivutLogger, "omatsivut", applicationType)
  private def audit = new AuditClass(omatSivutLogger, serviceName, applicationType)

  def log(auditEvent: AuditEvent) {
    audit.log(auditEvent.user, auditEvent.operation, auditEvent.target, auditEvent.changes)
  }
}

class OmatSivutLogger extends Logger with Logging {
  override def log(string : String): Unit = {
    logger.info(string)
  }
}





