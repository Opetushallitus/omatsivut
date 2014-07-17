package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.domain.{Hakemus, QuestionNode, ValidationError}

case class HakemusValidator(implicit val appConfig: AppConfig) extends Logging {
  def validate(hakemus: Hakemus): (List[ValidationError], List[QuestionNode]) = {
    try {
      ApplicationValidationWrapper().validate(hakemus)
    } catch {
      case e: Exception => {
        logger.error("There was an error validating application: " + hakemus.oid + " error was: " + e.getMessage, e)
        throw e
      }
    }
  }
}
