package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.validation.{ValidationInput, ValidationResult}
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.domain._

import scala.collection.JavaConversions._

case class ApplicationValidationWrapper(implicit val appConfig: AppConfig) extends Logging {
  private val applicationSystemService = appConfig.springContext.applicationSystemService
  private val dao = appConfig.springContext.applicationDAO
  private val validator = appConfig.springContext.validator

  def validate(hakemus: Hakemus): (List[ValidationError], List[Question]) = {
    val applicationSystem = applicationSystemService.getApplicationSystem(hakemus.haku.get.oid)
    val validationErrors: List[ValidationError] = validate(hakemus, applicationSystem)
    val requiredFieldErrors = validationErrors.filter(error => findError(error, "Pakollinen tieto.").isDefined)
    val questions: List[Question] = hakemus.hakutoiveet.flatMap { hakutoive =>
      RelatedQuestionHelper.findQuestionsByHakutoive(applicationSystem, hakutoive)
    }
    (validationErrors, questions)
  }

  private def validate(hakemus: Hakemus, applicationSystem: ApplicationSystem): List[ValidationError] = {
    val applications = dao.find(new Application().setOid(hakemus.oid)).toList
    if (applications.size > 1) throw new Error("Too many applications")
    val application = applications.head
    ApplicationUpdater.update(application, hakemus)
    val validationResult = validator.validate(convertToValidationInput(applicationSystem, application))
    convertoToValidationErrors(validationResult)
  }

  private def findError(error: ValidationError, searchText: String) = {
    error.translation.translations.find{ case (_, text) => text == searchText }
  }

  private def convertoToValidationErrors(validationResult: ValidationResult): List[ValidationError] = {
    validationResult.getErrorMessages.map { case (key, translations) =>
      ValidationError(key, Translations(translations.getTranslations.toMap))
    }.toList
  }

  private def convertToValidationInput(applicationSystem: ApplicationSystem, application: Application): ValidationInput = {
    new ValidationInput(applicationSystem.getForm, application.getVastauksetMerged, application.getOid, applicationSystem.getId)
  }
}
