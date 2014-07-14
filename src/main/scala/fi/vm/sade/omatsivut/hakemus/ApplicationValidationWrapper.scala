package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.domain.elements.Titled
import fi.vm.sade.haku.oppija.lomake.domain.elements.custom.SocialSecurityNumber
import fi.vm.sade.haku.oppija.lomake.domain.elements.questions.{DropdownSelect, TextQuestion, OptionQuestion => HakuOption, Radio => HakuRadio, TextArea => HakuTextArea}
import fi.vm.sade.haku.oppija.lomake.util.ElementTree
import fi.vm.sade.haku.oppija.lomake.validation.{ValidationInput, ValidationResult}
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.domain._

import scala.collection.JavaConversions._

case class ApplicationValidationWrapper(implicit val appConfig: AppConfig) extends Logging {
  private val applicationSystemService = appConfig.springContext.applicationSystemService
  private val dao = appConfig.springContext.applicationDAO
  private val validator = appConfig.springContext.validator

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

  def findMissingElements(hakemus: Hakemus): Option[List[Question]] = {
    try {
      val applicationSystem = applicationSystemService.getApplicationSystem(hakemus.haku.get.oid)
      val requiredFieldErrors = validate(hakemus, applicationSystem).filter(error => findError(error, "Pakollinen tieto.").isDefined)
      val form: ElementTree = new ElementTree(applicationSystem.getForm)
      val elements: List[Titled] = requiredFieldErrors.map(error => form.getChildById(error.key).asInstanceOf[Titled])
      Some(convertToQuestions(elements))
    } catch {
      case e: Exception => {
        logger.error("There was an error finding missing questions from application: " + hakemus.oid + "error was: " + e.getMessage)
        None
      }
    }
  }

  private def validate(hakemus: Hakemus, applicationSystem: ApplicationSystem): List[ValidationError] = {
    val applications = dao.find(new Application().setOid(hakemus.oid)).toList
    if (applications.size > 1) throw new Error("Too many applications")
    val application = applications.head
    val validationResult = validator.validate(convertToValidationInput(applicationSystem, application))
    convertoToValidationErrors(validationResult)
  }

  private def convertoToValidationErrors(validationResult: ValidationResult): List[ValidationError] = {
    validationResult.getErrorMessages.map { case (key, translations) =>
      ValidationError(key, Translations(translations.getTranslations.toMap))
    }.toList
  }

  private def convertToValidationInput(applicationSystem: ApplicationSystem, application: Application): ValidationInput = {
    new ValidationInput(applicationSystem.getForm, application.getVastauksetMerged, application.getOid, applicationSystem.getId)
  }

  private def findError(error: ValidationError, searchText: String) = {
    error.translation.translations.find{ case (_, text) => text == searchText }
  }

  private def getOptions(e: HakuOption): List[Choice] = {
    e.getOptions.map(o => Choice(getTitle(o), o.isDefaultOption)).toList
  }

  private def getTitle(e: Titled): Translations = {
    Translations(e.getI18nText.getTranslations.toMap)
  }

  private def convertToQuestions(elements: List[Titled]): List[Question with Product with Serializable] = {
    elements.flatMap { element => element match {
        case e: TextQuestion => List(Text(getTitle(e)))
        case e: HakuTextArea => List(TextArea(getTitle(e)))
        case e: HakuRadio => List(Radio(getTitle(e), getOptions(e)))
        case e: DropdownSelect => List(Dropdown(getTitle(e), getOptions(e)))
        case e: SocialSecurityNumber => List(Text(getTitle(e))) // Should never happen in prod
        case _ => {
          logger.error("Could not convert element of type: " + element.getType)
          Nil
        }
      }
    }
  }
}
