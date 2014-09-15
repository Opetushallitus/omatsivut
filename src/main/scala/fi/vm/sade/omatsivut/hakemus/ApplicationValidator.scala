package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.validation.{ValidationInput, ValidationResult}
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus._
import fi.vm.sade.omatsivut.hakemus.domain._
import fi.vm.sade.omatsivut.haku.AddedQuestionFinder
import fi.vm.sade.omatsivut.haku.domain.{AnswerId, QuestionNode}
import fi.vm.sade.omatsivut.util.Logging

import scala.collection.JavaConversions._

class ApplicationValidator(implicit val appConfig: AppConfig) extends Logging {
  private val dao = appConfig.springContext.applicationDAO
  private val validator = appConfig.springContext.validator
  private val hakemusRepository = appConfig.componentRegistry.hakemusRepository

  def validate(applicationSystem: ApplicationSystem)(hakemus: HakemusMuutos)(implicit lang: Language.Language): List[ValidationError] = {
    val storedApplication = hakemusRepository.findStoredApplication(hakemus)
    val updatedApplication = update(hakemus, applicationSystem, storedApplication)
    validateHakutoiveetAndAnswers(updatedApplication, storedApplication, applicationSystem) ++ errorsForUnknownAnswers(applicationSystem, hakemus)
  }

  def validateAndFindQuestions(applicationSystem: ApplicationSystem)(hakemus: HakemusMuutos, newKoulutusIds: List[String])(implicit lang: Language.Language): (List[ValidationError], List[QuestionNode], Application) = {
    withErrorLogging {
      val storedApplication = hakemusRepository.findStoredApplication(hakemus)
      val updatedApplication = update(hakemus, applicationSystem, storedApplication)
      val validationErrors: List[ValidationError] = validateHakutoiveetAndAnswers(updatedApplication, storedApplication, applicationSystem)
      val questions = AddedQuestionFinder.findQuestions(applicationSystem)(storedApplication, hakemus, newKoulutusIds)
      (validationErrors, questions, updatedApplication)
    } ("Error validating application: " + hakemus.oid)
  }

  private def validateHakutoiveetAndAnswers(updatedApplication: Application, storedApplication: Application, applicationSystem: ApplicationSystem)(implicit lang: Language.Language): List[ValidationError] = {
    if (isIncomplete(storedApplication)) {
      val errorsBeforeUpdate = validateAndConvertErrors(storedApplication, applicationSystem)
      val errorsAfterUpdate: List[ValidationError] = validateAndConvertErrors(updatedApplication, applicationSystem)
      errorsAfterUpdate.filter(!errorsBeforeUpdate.contains(_))
    } else {
      validateAndConvertErrors(updatedApplication, applicationSystem)
    }
  }

  private def isIncomplete(application: Application): Boolean = {
    application.getState == Application.State.INCOMPLETE
  }

  private def update(hakemus: HakemusMuutos, applicationSystem: ApplicationSystem, application: Application)(implicit lang: Language.Language) = {
    ApplicationUpdater.update(applicationSystem)(application.clone(), hakemus) // application is mutated
  }

  private def validateAndConvertErrors(application: Application, appSystem: ApplicationSystem)(implicit lang: Language.Language) = {
    val result = validator.validate(convertToValidationInput(appSystem, application))
    convertoToValidationErrors(result)
  }

  private def errorsForUnknownAnswers(applicationSystem: ApplicationSystem, hakemus: HakemusMuutos)(implicit lang: Language.Language): List[ValidationError] = {
    val application = hakemusRepository.findStoredApplication(hakemus)
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
