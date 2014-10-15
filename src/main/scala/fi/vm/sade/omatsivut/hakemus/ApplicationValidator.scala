package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.hakemus.it.dao.ApplicationDAO
import fi.vm.sade.haku.oppija.lomake.validation.{ElementTreeValidator, ValidationInput, ValidationResult}
import fi.vm.sade.omatsivut.config.SpringContextComponent
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus._
import fi.vm.sade.omatsivut.hakemus.domain._
import fi.vm.sade.omatsivut.haku.AddedQuestionFinder
import fi.vm.sade.omatsivut.haku.domain.{AnswerId, Lomake, QuestionNode}
import fi.vm.sade.omatsivut.util.Logging

import scala.collection.JavaConversions._

trait ApplicationValidatorComponent {
  this: SpringContextComponent with HakemusRepositoryComponent =>

  val hakemusRepository: HakemusRepository
  def newApplicationValidator: ApplicationValidator

  class ApplicationValidator extends Logging {
    private val dao: ApplicationDAO = springContext.applicationDAO
    private val validator: ElementTreeValidator = springContext.validator

    def validate(lomake: Lomake)(hakemus: HakemusMuutos)(implicit lang: Language.Language): List[ValidationError] = {
      val storedApplication = hakemusRepository.findStoredApplicationByOid(hakemus.oid)
      val updatedApplication = update(hakemus, lomake, storedApplication)
      validateHakutoiveetAndAnswers(updatedApplication, storedApplication, lomake) ++ errorsForUnknownAnswers(lomake, hakemus)
    }

    def validateAndFindQuestions(lomake: Lomake)(hakemus: HakemusMuutos, newKoulutusIds: List[String], personOid: String)(implicit lang: Language.Language): (List[ValidationError], List[QuestionNode], Application) = {
      withErrorLogging {
        val storedApplication = hakemusRepository.findStoredApplicationByOid(hakemus.oid)
        if (storedApplication.getPersonOid != personOid) throw new IllegalArgumentException("personId mismatch")
        val updatedApplication = update(hakemus, lomake, storedApplication)
        val validationErrors: List[ValidationError] = validateHakutoiveetAndAnswers(updatedApplication, storedApplication, lomake)
        val questions = AddedQuestionFinder.findQuestions(lomake)(storedApplication, hakemus, newKoulutusIds)
        (validationErrors, questions, updatedApplication)
      } ("Error validating application: " + hakemus.oid)
    }

    private def validateHakutoiveetAndAnswers(updatedApplication: Application, storedApplication: Application, lomake: Lomake)(implicit lang: Language.Language): List[ValidationError] = {
      if (isIncomplete(storedApplication)) {
        val errorsBeforeUpdate = validateAndConvertErrors(storedApplication, lomake)
        val errorsAfterUpdate: List[ValidationError] = validateAndConvertErrors(updatedApplication, lomake)
        errorsAfterUpdate.filter(!errorsBeforeUpdate.contains(_))
      } else {
        validateAndConvertErrors(updatedApplication, lomake)
      }
    }

    private def isIncomplete(application: Application): Boolean = {
      application.getState == Application.State.INCOMPLETE
    }

    private def update(hakemus: HakemusMuutos, lomake: Lomake, application: Application)(implicit lang: Language.Language): Application = {
      ApplicationUpdater.update(lomake)(application.clone(), hakemus) // application is mutated
    }

    private def validateAndConvertErrors(application: Application, appSystem: Lomake)(implicit lang: Language.Language) = {
      val result = validator.validate(convertToValidationInput(appSystem, application))
      convertoToValidationErrors(result)
    }

    private def errorsForUnknownAnswers(lomake: Lomake, hakemus: HakemusMuutos)(implicit lang: Language.Language): List[ValidationError] = {
      val application = hakemusRepository.findStoredApplicationByOid(hakemus.oid)
      val allAnswers: Answers = ApplicationUpdater.getAllAnswersForApplication(lomake, application, hakemus)
      val acceptedAnswerIds: Seq[AnswerId] = AddedQuestionFinder.findAddedQuestions(lomake, allAnswers, Hakemus.emptyAnswers).flatMap(_.answerIds).toList

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

    private def convertToValidationInput(lomake: Lomake, application: Application): ValidationInput = {
      new ValidationInput(lomake.form, application.getVastauksetMerged, application.getOid, lomake.oid)
    }
  }
}

