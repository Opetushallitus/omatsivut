package fi.vm.sade.hakemuseditori.hakemus

import fi.vm.sade.hakemuseditori.hakemus.HakemusInfo.{ApplicationOptionOid, ShouldPay}
import fi.vm.sade.hakemuseditori.hakemus.domain.{Hakemus, ValidationError}
import fi.vm.sade.hakemuseditori.lomake.domain.QuestionNode

case class HakemusInfo(hakemus: Hakemus, errors: List[ValidationError], questions: List[QuestionNode], tulosOk: Boolean, kelaURL: Option[String], paymentInfo: Option[Map[ApplicationOptionOid, ShouldPay]])

object HakemusInfo {
  type ApplicationOptionOid = String
  type ShouldPay = Boolean

  def apply(hakemus: Hakemus, errors: List[ValidationError], questions: List[QuestionNode], kelaURL: Option[String], paymentInfo: Option[Map[ApplicationOptionOid, ShouldPay]]): HakemusInfo = {
    HakemusInfo(hakemus, errors, questions, tulosOk = true, kelaURL = kelaURL, paymentInfo)
  }
}