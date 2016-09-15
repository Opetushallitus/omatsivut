package fi.vm.sade.hakemuseditori.hakemus

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.hakemus.HakemusInfo.{ApplicationOptionOid, ShouldPay}
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus._
import fi.vm.sade.hakemuseditori.hakemus.domain._
import fi.vm.sade.hakemuseditori.hakumaksu.HakumaksuComponent
import fi.vm.sade.hakemuseditori.localization.TranslationsComponent
import fi.vm.sade.hakemuseditori.lomake.{LomakeRepositoryComponent, AddedQuestionFinder}
import fi.vm.sade.hakemuseditori.lomake.domain.{AnswerId, Lomake}
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaComponent
import fi.vm.sade.hakemuseditori.tarjonta.domain.Haku
import fi.vm.sade.hakemuseditori.user.User
import fi.vm.sade.haku.oppija.hakemus.it.dao.ApplicationDAO
import fi.vm.sade.haku.oppija.lomake.validation.ValidationInput.ValidationContext
import fi.vm.sade.haku.oppija.lomake.validation.{ElementTreeValidator, ValidationInput, ValidationResult}
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants._
import fi.vm.sade.utils.slf4j.Logging

import scala.collection.JavaConversions._

trait ApplicationValidatorComponent {
  this: SpringContextComponent with HakemusConverterComponent with HakemusRepositoryComponent with TarjontaComponent
    with TranslationsComponent with HakumaksuComponent with LomakeRepositoryComponent =>

  def newApplicationValidator: ApplicationValidator

  class ApplicationValidator extends Logging {
    private val dao: ApplicationDAO = springContext.applicationDAO
    private val validator: ElementTreeValidator = springContext.validator

    def validate(lomake: Lomake, hakemusMuutos: HakemusMuutos, haku: Haku)(implicit lang: Language.Language): List[ValidationError] = {
      val storedApplication = applicationRepository.findStoredApplicationByOid(hakemusMuutos.oid).getOrElse(throw new RuntimeException(s"Application ${hakemusMuutos.oid} not found"))
      val updatedApplication = update(hakemusMuutos, lomake, storedApplication)
      validateHakutoiveetAndAnswers(updatedApplication, storedApplication, lomake) ++
        errorsForUnknownAnswers(lomake, hakemusMuutos) ++
        errorsForEditingInactiveHakuToive(updatedApplication, storedApplication, haku, lomake)
    }

    def validateAndFindQuestions(lomake: Lomake, hakemusMuutos: HakemusMuutos, haku: Haku, user: User)(implicit lang: Language.Language): HakemusInfo = {
      withErrorLogging {
        val storedApplication = applicationRepository.findStoredApplicationByOid(hakemusMuutos.oid).getOrElse(throw new RuntimeException(s"Application ${hakemusMuutos.oid} not found"))

        user.checkAccessToUserData(storedApplication.personOid)

        val paymentInfo = getPaymentInfo(storedApplication.phaseAnswers(PHASE_EDUCATION), hakemusMuutos.preferences, haku.oid)

        validateAndFindQuestions(haku, lomake, hakemusMuutos, storedApplication) match {
          case (app, errors, questions) => HakemusInfo(hakemusConverter.convertToHakemus(None, Some(lomake), haku, app), errors, questions, Some(paymentInfo))
        }
      } ("Error validating application: " + hakemusMuutos.oid)
    }

    private def getPaymentInfo(educationAnswers: Map[String, String], preferences: List[HakutoiveData], applicationSystemOid: String): Map[ApplicationOptionOid, ShouldPay] = {
      def hakuAppApplicationOptionKey(index: Int) = PREFERENCE_PREFIX + index + OPTION_ID_POSTFIX
      val applicationOptionAnswers = preferences.zipWithIndex.map {
        case (p, index) => hakuAppApplicationOptionKey(index) -> p.getOrElse(PREFERENCE_FRAGMENT_OPTION_ID, "")
      }.toMap

      if (lomakeRepository.isMaksumuuriKaytossa(applicationSystemOid)) {
        hakumaksuService.getPaymentRequirementsForApplicationOptions(educationAnswers ++ applicationOptionAnswers).toMap.map {
          case (key, shouldPay) => key.toString -> Boolean.unbox(shouldPay)
        }
      } else {
        applicationOptionAnswers.collect {
          case (_, aoId) if aoId.nonEmpty => aoId -> false
        }
      }
    }

    private[hakemus] def validateAndFindQuestions(haku: Haku, lomake: Lomake, newHakemus: HakemusLike, storedApplication: ImmutableLegacyApplicationWrapper)(implicit lang: Language.Language) = {
      withErrorLogging {
        val updatedApplication = update(newHakemus, lomake, storedApplication)
        val validationErrors: List[ValidationError] = validateHakutoiveetAndAnswers(updatedApplication, storedApplication, lomake) ++
          errorsForEditingInactiveHakuToive(updatedApplication, storedApplication, haku, lomake)
        val questions = AddedQuestionFinder.findQuestions(lomake)(storedApplication, newHakemus, tarjontaService.filterHakutoiveOidsByActivity(true, newHakemus.preferences, haku))
        (updatedApplication, validationErrors, questions)
      } ("Error validating application: " + newHakemus.oid)
    }

    private def validateHakutoiveetAndAnswers(updatedApplication: ImmutableLegacyApplicationWrapper, storedApplication: ImmutableLegacyApplicationWrapper, lomake: Lomake)(implicit lang: Language.Language): List[ValidationError] = {
      val errorsBeforeUpdate = validateAndConvertErrors(storedApplication, lomake)
      val errorsAfterUpdate: List[ValidationError] = validateAndConvertErrors(updatedApplication, lomake)
      errorsAfterUpdate.filter(!errorsBeforeUpdate.contains(_))
    }

    private def update(hakemusMuutos: HakemusLike, lomake: Lomake, application: ImmutableLegacyApplicationWrapper)(implicit lang: Language.Language): ImmutableLegacyApplicationWrapper = {
      application.copy(answers = AnswerHelper.getUpdatedAnswersForApplication(lomake, application, hakemusMuutos))
    }

    private def validateAndConvertErrors(application: ImmutableLegacyApplicationWrapper, appSystem: Lomake)(implicit lang: Language.Language) = {
      val result = validator.validate(convertToValidationInput(appSystem, application))
      convertoToValidationErrors(result)
    }

    private def errorsForEditingInactiveHakuToive(updatedApplication: ImmutableLegacyApplicationWrapper, storedApplication: ImmutableLegacyApplicationWrapper, haku: Haku, lomake: Lomake)(implicit lang: Language.Language): List[ValidationError] = {
      val oldHakuToiveet = HakutoiveetConverter.convertFromAnswers(storedApplication.answers, Some(lomake.maxHakutoiveet))
      val newHakuToiveet = HakutoiveetConverter.convertFromAnswers(updatedApplication.answers.mapValues(_.toMap), Some(lomake.maxHakutoiveet))
      val oldInactiveHakuToiveet: List[String] = tarjontaService.filterHakutoiveOidsByActivity(false, oldHakuToiveet, haku)
      val newInactiveHakuToiveet: List[String] = tarjontaService.filterHakutoiveOidsByActivity(false, newHakuToiveet, haku)
      val newHakutoiveetWithIndex = newHakuToiveet.zipWithIndex

      val addedInActiveHakutoiveet = newInactiveHakuToiveet.filter(!oldInactiveHakuToiveet.contains(_))

      val errorsForAdded = addedInActiveHakutoiveet.flatMap { hakukohdeOid =>
        newHakutoiveetWithIndex.find { case (hakutoive: HakutoiveData, index: Int) =>
          hakutoive.get("Koulutus-id").map { _ == hakukohdeOid }.getOrElse(false)
        }.map(_._2)
      }.map((index) => new ValidationError("preference"+(index+1) + "-Koulutus", translations.getTranslation("error", "applicationPeriodNotActive")))

      val errorsForOtherModifications =
        if (newInactiveHakuToiveet != oldInactiveHakuToiveet) {
          logger.warn("newInactiveHakuToiveet != oldInactiveHakuToiveet:" + newInactiveHakuToiveet + " vrs. " + oldInactiveHakuToiveet)
          List(new ValidationError("koulutus-id", translations.getTranslation("error", "applicationPeriodNotActive")))
        }
        else
          List.empty

      if (errorsForAdded.size != 0)
        errorsForAdded
      else
        errorsForOtherModifications
    }

    private def errorsForUnknownAnswers(lomake: Lomake, hakemusMuutos: HakemusMuutos)(implicit lang: Language.Language): List[ValidationError] = {
      val application = applicationRepository.findStoredApplicationByOid(hakemusMuutos.oid).getOrElse(throw new RuntimeException(s"Application ${hakemusMuutos.oid} not found"))
      val allAnswers: Answers = AnswerHelper.getAllAnswersForApplication(lomake, application, hakemusMuutos)
      val acceptedAnswerIds: Seq[AnswerId] = AddedQuestionFinder.findAddedQuestions(lomake, allAnswers, Hakemus.emptyAnswers).flatMap(_.answerIds).toList

      val flatAnswers: List[(String, String, String)] = hakemusMuutos.answers.toList.flatMap {
        case (phaseId, groupAnswers) =>
          groupAnswers.toList.map { case (questionId, answer) =>
            (phaseId, questionId, answer)
          }
      }
      val unknownAnswers: List[(String, String, String)] = flatAnswers
        .filterNot {
          case (PHASE_PERSONAL, questionId, _) if List(ELEMENT_ID_FIN_ADDRESS, ELEMENT_ID_FIN_POSTAL_NUMBER, ELEMENT_ID_EMAIL, ELEMENT_ID_COUNTRY_OF_RESIDENCY).contains(questionId) || questionId.startsWith(ELEMENT_ID_PREFIX_PHONENUMBER) => true
          case (phaseId, questionId, _) => acceptedAnswerIds.contains(AnswerId(phaseId, questionId))
        }
      unknownAnswers
        .map{ case (phaseId, questionId, answer) =>{
          logger.warn(acceptedAnswerIds.toString())
          ValidationError(questionId, "unknown answer id")}
        }
    }

    private def convertoToValidationErrors(validationResult: ValidationResult)(implicit lang: Language.Language) : List[ValidationError] = {
      validationResult.getErrorMessages.map { case (key, translations) =>
        ValidationError(key, translations.getText(lang.toString))
      }.toList
    }

    private def convertToValidationInput(lomake: Lomake, application: ImmutableLegacyApplicationWrapper): ValidationInput = {
      new ValidationInput(lomake.form, FlatAnswers.flatten(application.answers), application.oid, lomake.oid, ValidationContext.applicant_modify)
    }
  }
}

