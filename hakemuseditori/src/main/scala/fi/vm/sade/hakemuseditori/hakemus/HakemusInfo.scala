package fi.vm.sade.hakemuseditori.hakemus
import fi.vm.sade.hakemuseditori.hakemus.domain.{Hakemus, ValidationError}
import fi.vm.sade.hakemuseditori.lomake.domain.QuestionNode

case class HakemusInfo(hakemus: Hakemus, errors: List[ValidationError], questions: List[QuestionNode], tulosOk: Boolean)

object HakemusInfo {
  def apply(hakemus: Hakemus, errors: List[ValidationError], questions: List[QuestionNode]): HakemusInfo = {
    HakemusInfo(hakemus, errors, questions, tulosOk = true)
  }
}