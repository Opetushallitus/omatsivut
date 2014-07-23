package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.validation.{ValidationInput, ValidationResult}
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.domain.Hakemus._
import fi.vm.sade.omatsivut.domain._

import scala.collection.JavaConversions._

case class ApplicationValidator(implicit val appConfig: AppConfig) extends Logging {
  private val dao = appConfig.springContext.applicationDAO
  private val validator = appConfig.springContext.validator
  val preferencePhaseKey = OppijaConstants.PHASE_APPLICATION_OPTIONS

  def validate(applicationSystem: ApplicationSystem)(hakemus: Hakemus): (List[ValidationError], List[QuestionNode]) = {
    withErrorLogging {
      val validationErrors: List[ValidationError] = justValidate(hakemus, applicationSystem)
      val storedApplication = findStoredApplication(hakemus)

      val questionsPerHakutoive: List[QuestionNode] = hakemus.hakutoiveet.filterNot(applicationContains(storedApplication)).flatMap { hakutoive =>
        val groupedQuestions: Seq[QuestionNode] = AddedQuestionFinder.findQuestionsByHakutoive(applicationSystem, hakutoive)
        groupedQuestions match {
          case Nil => Nil
          case _ => List(QuestionGroup(HakutoiveetConverter.describe(hakutoive), groupedQuestions.toList))
        }
      }
      (validationErrors, questionsPerHakutoive)
    } ("Error validating application: " + hakemus.oid)
  }

  private def applicationContains(application: Application)(hakutoive: Hakutoive) = {
    HakutoiveetConverter.answersContainHakutoive(application.getAnswers.get(preferencePhaseKey).toMap, hakutoive)
  }

  private def justValidate(hakemus: Hakemus, applicationSystem: ApplicationSystem): List[ValidationError] = {
    val application: Application = findStoredApplication(hakemus) // <- needs to be fetched here because is mutated below
    ApplicationUpdater.update(applicationSystem)(application, hakemus)
    val validationResult = validator.validate(convertToValidationInput(applicationSystem, application))
    convertoToValidationErrors(validationResult) ++ errorsForUnknownAnswers(applicationSystem, application, hakemus)
  }

  private def errorsForUnknownAnswers(applicationSystem: ApplicationSystem, application: Application, hakemus: Hakemus): List[ValidationError] = {
    val allAnswers = ApplicationUpdater.getAllAnswersForApplication(applicationSystem, application, hakemus)
    val questionIds= AddedQuestionFinder.findAddedQuestions(applicationSystem, allAnswers, Hakemus.emptyAnswers).flatMap(_.flatten).flatMap(_.answerIds)

    val flatAnswers: List[(String, String, String)] = hakemus.answers.toList.flatMap {
      case (phaseId, groupAnswers) =>
        groupAnswers.toList.map { case (questionId, answer) =>
          (phaseId, questionId, answer)
        }
    }
    flatAnswers
      .filterNot { case (phaseId, questionId, _) => questionIds.contains(QuestionId(phaseId, questionId))}
      .map{ case (phaseId, questionId, answer) => ValidationError(questionId, "unknown question")}
  }

  def findStoredApplication(hakemus: Hakemus): Application = {
    val applications = dao.find(new Application().setOid(hakemus.oid)).toList
    if (applications.size > 1) throw new RuntimeException("Too many applications for oid " + hakemus.oid)
    if (applications.size == 0) throw new RuntimeException("Application not found for oid " + hakemus.oid)
    val application = applications.head
    application
  }

  private def convertoToValidationErrors(validationResult: ValidationResult): List[ValidationError] = {
    validationResult.getErrorMessages.map { case (key, translations) =>
      ValidationError(key, translations.getTranslations.get("fi")) // TODO: kieliversiot
    }.toList
  }

  private def convertToValidationInput(applicationSystem: ApplicationSystem, application: Application): ValidationInput = {
    new ValidationInput(applicationSystem.getForm, application.getVastauksetMerged, application.getOid, applicationSystem.getId)
  }
}
