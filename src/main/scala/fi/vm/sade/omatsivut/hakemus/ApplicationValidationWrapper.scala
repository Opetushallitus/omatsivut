package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.validation.{ValidationInput, ValidationResult}
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.domain._
import fi.vm.sade.omatsivut.domain.Hakemus._

import scala.collection.JavaConversions._

case class ApplicationValidationWrapper(implicit val appConfig: AppConfig) extends Logging {
  private val applicationSystemService = appConfig.springContext.applicationSystemService
  private val dao = appConfig.springContext.applicationDAO
  private val validator = appConfig.springContext.validator

  def validate(hakemus: Hakemus): (List[ValidationError], List[QuestionNode]) = {
    val applicationSystem = applicationSystemService.getApplicationSystem(hakemus.haku.get.oid)
    val validationErrors: List[ValidationError] = validate(hakemus, applicationSystem)
    val storedApplication = findStoredApplication(hakemus)

    val questions: List[QuestionNode] = hakemus.hakutoiveet.filterNot(applicationContains(storedApplication)).flatMap { hakutoive =>
      val questions: Seq[QuestionNode] = RelatedQuestionHelper.findQuestionsByHakutoive(applicationSystem, hakutoive)
      questions match {
        case Nil => Nil
        case _ => List(QuestionGroup(hakutoive.getOrElse("Koulutus", ""), questions.toList))
      }
    }
    (validationErrors, questions)
  }

  private def applicationContains(application: Application)(hakutoive: Hakutoive) = {
    val opetusPiste = hakutoive.get("Opetuspiste-id")
    val koulutus = hakutoive.get("Koulutus-id")
    //application.getAnswers.get("hakutoiveet").toMap.
    false
  }

  private def validate(hakemus: Hakemus, applicationSystem: ApplicationSystem): List[ValidationError] = {
    val application: Application = findStoredApplication(hakemus)
    ApplicationUpdater.update(application, hakemus)
    val validationResult = validator.validate(convertToValidationInput(applicationSystem, application))
    convertoToValidationErrors(validationResult)
  }

  def findStoredApplication(hakemus: Hakemus): Application = {
    val applications = dao.find(new Application().setOid(hakemus.oid)).toList
    if (applications.size > 1) throw new RuntimeException("Too many applications for oid " + hakemus.oid)
    if (applications.size == 0) throw new RuntimeException("Applicadtion not found for oid " + hakemus.oid)
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
