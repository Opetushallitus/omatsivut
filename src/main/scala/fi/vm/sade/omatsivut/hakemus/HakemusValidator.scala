package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.domain.{ValidationError, Hakemus}

object HakemusValidator {
  def validate(hakemus: Hakemus): List[ValidationError] = {
    ApplicationValidationWrapper.verify(hakemus) match {
      case Some(e) => e
      case None => List()
    }
  }
}
