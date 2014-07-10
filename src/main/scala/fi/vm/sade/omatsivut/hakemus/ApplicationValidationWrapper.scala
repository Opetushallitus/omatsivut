package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.hakemus.it.dao.ApplicationDAO
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.service.ApplicationSystemService
import fi.vm.sade.haku.oppija.lomake.validation.{ValidationResult, ElementTreeValidator, ValidationInput}
import fi.vm.sade.omatsivut.domain.{Hakemus, Translations, ValidationError}
import fi.vm.sade.omatsivut.{Logging, OmatSivutSpringContext}

import scala.collection.JavaConversions._

object ApplicationValidationWrapper extends Logging {
  private val applicationSystemService: ApplicationSystemService = OmatSivutSpringContext.context.getBean(classOf[ApplicationSystemService])
  private val dao: ApplicationDAO = OmatSivutSpringContext.context.getBean(classOf[ApplicationDAO])
  private val validator: ElementTreeValidator = OmatSivutSpringContext.context.getBean(classOf[ElementTreeValidator])

  def verify(hakemus: Hakemus): Option[List[ValidationError]] = {
    try {
      val applicationSystem = applicationSystemService.getApplicationSystem(hakemus.haku.get.oid)
      val applications = dao.find(new Application().setOid(hakemus.oid)).toList
      if(applications.size > 1) throw new Error("Too many applications")
      val application = applications.head
      val validationResult = validator.validate(convertToValidationInput(applicationSystem, application))
      Some(convertoToValidationErrors(validationResult))
    } catch {
      case e: Exception => {
        logger.error("There was an error validating application: " + hakemus.oid + "error was: " + e.getMessage)
        None
      }
    }
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
