package fi.vm.sade.hakemuseditori.hakemus

import fi.vm.sade.hakemuseditori.hakemus.HakemusInfo.{ApplicationOptionOid, ShouldPay}
import fi.vm.sade.hakemuseditori.hakemus.domain.{Hakemus, ValidationError}
import fi.vm.sade.hakemuseditori.lomake.domain.QuestionNode

case class HakemusInfo(hakemus: Hakemus,
                       errors: List[ValidationError],
                       questions: List[QuestionNode],
                       tulosOk: Boolean,
                       paymentInfo: Option[Map[ApplicationOptionOid, ShouldPay]],
                       hakemusSource: String,
                       ataruHakijaUrl: String) {
  def withoutKelaUrl: HakemusInfo = copy(hakemus = hakemus.withoutKelaUrl)
}

object HakemusInfo {
  type ApplicationOptionOid = String
  type ShouldPay = Boolean
}
