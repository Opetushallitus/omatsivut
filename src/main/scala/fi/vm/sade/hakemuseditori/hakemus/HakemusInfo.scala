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
                       ataruHakijaUrl: String)

object HakemusInfo {
  type ApplicationOptionOid = String
  type ShouldPay = Boolean

  def apply(hakemus: Hakemus,
            errors: List[ValidationError],
            questions: List[QuestionNode],
            paymentInfo: Option[Map[ApplicationOptionOid, ShouldPay]],
            hakemusSource: String = "HakuApp",
            ataruHakijaUrl: String = ""): HakemusInfo = {
    HakemusInfo(hakemus, errors, questions, tulosOk = true, paymentInfo, hakemusSource, ataruHakijaUrl)
  }
}

case class ApplicationsResponse(allApplicationsFetched: Boolean, applications: List[HakemusInfo])
