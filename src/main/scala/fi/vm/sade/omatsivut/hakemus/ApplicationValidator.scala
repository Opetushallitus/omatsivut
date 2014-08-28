package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.util.Logging

import scala.collection.JavaConversions._
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.validation.{ValidationInput, ValidationResult}
import AppConfig.AppConfig
import fi.vm.sade.omatsivut.hakemus.domain._
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus._

case class ApplicationValidator(implicit val appConfig: AppConfig) extends Logging {
  private val dao = appConfig.springContext.applicationDAO
  private val validator = appConfig.springContext.validator

  def validate(applicationSystem: ApplicationSystem)(hakemus: Hakemus)(implicit lang: Language.Language): List[ValidationError] = {
    val storedApplication = HakemusRepository().findStoredApplication(hakemus)
    validateHakutoiveetAndAnswers(hakemus, storedApplication, applicationSystem) ++ errorsForUnknownAnswers(applicationSystem, hakemus)
  }

  def validateAndFindQuestions(applicationSystem: ApplicationSystem)(hakemus: Hakemus, newKoulutusIds: List[String])(implicit lang: Language.Language): (List[ValidationError], List[QuestionNode]) = {
    withErrorLogging {
      val storedApplication = HakemusRepository().findStoredApplication(hakemus)
      val validationErrors: List[ValidationError] = validateHakutoiveetAndAnswers(hakemus, storedApplication, applicationSystem)
      val questions = AddedQuestionFinder.findQuestions(applicationSystem)(storedApplication, hakemus, newKoulutusIds)
      (validationErrors, questions)
    } ("Error validating application: " + hakemus.oid)
  }

  private def validateHakutoiveetAndAnswers(hakemus: Hakemus, storedApplication: Application, applicationSystem: ApplicationSystem)(implicit lang: Language.Language): List[ValidationError] = {
    if (isIncomplete(storedApplication)) {
      val errorsBeforeUpdate = validateAndConvertErrors(storedApplication, applicationSystem)
      val errorsAfterUpdate: List[ValidationError] = updateAndValidate(hakemus, applicationSystem, storedApplication.clone())
      errorsAfterUpdate.filter(!errorsBeforeUpdate.contains(_))
    } else {
      updateAndValidate(hakemus, applicationSystem, storedApplication.clone())
    }
  }

  private def isIncomplete(application: Application): Boolean = {
    application.getState == Application.State.INCOMPLETE
  }

  private def updateAndValidate(hakemus: Hakemus, applicationSystem: ApplicationSystem, application: Application)(implicit lang: Language.Language): List[ValidationError] = {
    ApplicationUpdater.update(applicationSystem)(application, hakemus) // application is mutated
    validateAndConvertErrors(application, applicationSystem)
  }

  private def validateAndConvertErrors(application: Application, appSystem: ApplicationSystem)(implicit lang: Language.Language) = {
    val result = validator.validate(convertToValidationInput(appSystem, application))
    convertoToValidationErrors(result)
  }

  private def errorsForUnknownAnswers(applicationSystem: ApplicationSystem, hakemus: Hakemus)(implicit lang: Language.Language): List[ValidationError] = {
    val application = HakemusRepository().findStoredApplication(hakemus)
    val allAnswers: Answers = ApplicationUpdater.getAllAnswersForApplication(applicationSystem, application, hakemus)
    val acceptedAnswerIds: Seq[AnswerId] = AddedQuestionFinder.findAddedQuestions(applicationSystem, allAnswers, Hakemus.emptyAnswers).flatMap(_.answerIds).toList

    val flatAnswers: List[(String, String, String)] = hakemus.answers.toList.flatMap {
      case (phaseId, groupAnswers) =>
        groupAnswers.toList.map { case (questionId, answer) =>
          (phaseId, questionId, answer)
        }
    }
    val unknownAnswers: List[(String, String, String)] = flatAnswers
      .filterNot { case (phaseId, questionId, _) => acceptedAnswerIds.contains(AnswerId(phaseId, questionId))}
    unknownAnswers
      .map{ case (phaseId, questionId, answer) =>
        ValidationError(questionId, "unknown answer id")
      }
  }

  private def convertoToValidationErrors(validationResult: ValidationResult)(implicit lang: Language.Language) : List[ValidationError] = {
    validationResult.getErrorMessages.map { case (key, translations) =>
      ValidationError(key, translations.getTranslations.get(lang.toString))
    }.toList
  }

  private def convertToValidationInput(applicationSystem: ApplicationSystem, application: Application): ValidationInput = {
    new ValidationInput(applicationSystem.getForm, application.getVastauksetMerged, application.getOid, applicationSystem.getId)
  }
}
