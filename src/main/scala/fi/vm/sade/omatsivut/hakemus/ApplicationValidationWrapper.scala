package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.domain.elements.Titled
import fi.vm.sade.haku.oppija.lomake.domain.elements.questions.{DropdownSelect, TextQuestion, OptionQuestion => HakuOption, Radio => HakuRadio, TextArea => HakuTextArea}
import fi.vm.sade.haku.oppija.lomake.util.ElementTree
import fi.vm.sade.haku.oppija.lomake.validation.{ValidationInput, ValidationResult}
import fi.vm.sade.omatsivut.domain._
import fi.vm.sade.omatsivut.{Logging, OmatSivutSpringContext}

import scala.collection.JavaConversions._

object ApplicationValidationWrapper extends Logging {
  private val applicationSystemService = OmatSivutSpringContext.context.applicationSystemService
  private val dao = OmatSivutSpringContext.context.applicationDAO
  private val validator = OmatSivutSpringContext.context.validator

  def validate(hakemus: Hakemus): Option[List[ValidationError]] = {
    try {
      val applicationSystem = applicationSystemService.getApplicationSystem(hakemus.haku.get.oid)
      Some(validate(hakemus, applicationSystem))
    } catch {
      case e: Exception => {
        logger.error("There was an error validating application: " + hakemus.oid + "error was: " + e.getMessage)
        None
      }
    }
  }

  def validate(hakemus: Hakemus, applicationSystem: ApplicationSystem): List[ValidationError] = {
    val applications = dao.find(new Application().setOid(hakemus.oid)).toList
    if (applications.size > 1) throw new Error("Too many applications")
    val application = applications.head
    val validationResult = validator.validate(convertToValidationInput(applicationSystem, application))
    convertoToValidationErrors(validationResult)
  }

  def findMissingElements(hakemus: Hakemus): List[Question] = {
    val applicationSystem = applicationSystemService.getApplicationSystem(hakemus.haku.get.oid)
    val requiredFieldErrors = validate(hakemus, applicationSystem).filter(error => findError(error, "Pakollinen tieto.").isDefined)
    val form: ElementTree = new ElementTree(applicationSystem.getForm)
    val elements: List[Titled] = requiredFieldErrors.map(error => form.getChildById(error.key).asInstanceOf[Titled])
    elements.flatMap { element => element match {
        case e: TextQuestion => List(Text(getTitle(e)))
        case e: HakuTextArea => List(TextArea(getTitle(e)))
        case e: HakuRadio => List(Radio(getTitle(e), getOptions(e)))
        case e: DropdownSelect => List(Dropdown(getTitle(e), getOptions(e)))
        case _ => Nil
      }
    }
  }

  private def getOptions(e: HakuOption): List[Translations] = {
    e.getOptions.map(o => getTitle(o)).toList
  }

  private def getTitle(e: Titled): Translations = {
    Translations(e.getI18nText.getTranslations.toMap)
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
