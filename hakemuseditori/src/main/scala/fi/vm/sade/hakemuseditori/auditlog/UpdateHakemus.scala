package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.Answers
import fi.vm.sade.hakemuseditori.user.User

case class UpdateHakemus(user: User, hakemusOid: String, hakuOid: String, originalAnswers: Answers, updatedAnswers: Answers, target: String = "Hakemus") extends AuditEvent {
  def toLogMessage = "Tallennettu päivitetty hakemus haussa " + hakuOid + ": " + hakemusOid + ", " + user.toString
}
