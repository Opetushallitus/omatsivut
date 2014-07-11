package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.domain.{ValidationError, Hakemus}

case class HakemusValidator(implicit val appConfig: AppConfig) {
  def validate(hakemus: Hakemus): List[ValidationError] = {
    ApplicationValidationWrapper().validate(hakemus) match {
      case Some(e) => e
      case None => List()
    }
  }

  def findMissingQuestions(hakemus: Hakemus) = {
    ApplicationValidationWrapper().findMissingElements(hakemus) match {
      case Some(e) => e
      case None => List()
    }
  }
}
