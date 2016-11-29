package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.Answers
import fi.vm.sade.hakemuseditori.user.{Oppija, User}

case class UpdateHakemus(user: User, hakemusOid: String, hakuOid: String, originalAnswers: Answers, updatedAnswers: Answers, target: String = "Hakemus") extends AuditEvent {
  def isUserOppija = user match {
    case u: Oppija => true
    case _ => false
  }
  def toLogMessage = Map("message" -> "Tallennettu päivitetty hakemus haussa", "hakuOid" -> hakuOid, "hakemusOid" -> hakemusOid, "id" -> user.oid)
}
