package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.hakemus.it.dao.ApplicationDAO
import fi.vm.sade.haku.oppija.lomake.validation.{ElementTreeValidator, ValidationInput, ValidationResult}
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.config.SpringContextComponent
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus._
import fi.vm.sade.omatsivut.hakemus.domain._
import fi.vm.sade.omatsivut.localization.Translations
import fi.vm.sade.omatsivut.lomake.AddedQuestionFinder
import fi.vm.sade.omatsivut.lomake.domain.{AnswerId, Lomake, QuestionNode}
import fi.vm.sade.omatsivut.servlet.HakemusInfo
import fi.vm.sade.omatsivut.tarjonta.{Haku, Hakukohde, TarjontaComponent}
import fi.vm.sade.omatsivut.util.Logging

import scala.collection.JavaConversions._

trait ApplicationValidatorComponent {
  this: SpringContextComponent with HakemusConverterComponent with HakemusRepositoryComponent with TarjontaComponent =>

  def newApplicationValidator: ApplicationValidator

  class ApplicationValidator extends Logging {
    private val dao: ApplicationDAO = springContext.applicationDAO
    private val validator: ElementTreeValidator = springContext.validator

    def validate(lomake: Lomake, hakemusMuutos: HakemusMuutos, haku: Haku)(implicit lang: Language.Language): List[ValidationError] = {
      val storedApplication = hakemusRepository.findStoredApplicationByOid(hakemusMuutos.oid)
      val updatedApplication = update(hakemusMuutos, lomake, storedApplication)
      validateHakutoiveetAndAnswers(updatedApplication, storedApplication, lomake) ++
        errorsForUnknownAnswers(lomake, hakemusMuutos) ++
        errorsForEditingInactiveHakuToive(updatedApplication, storedApplication, haku)
    }

    def validateAndFindQuestions(lomake: Lomake, hakemusMuutos: HakemusMuutos, haku: Haku, personOid: String)(implicit lang: Language.Language): HakemusInfo = {
      withErrorLogging {
        val storedApplication = hakemusRepository.findStoredApplicationByOid(hakemusMuutos.oid)
        validateAndFindQuestions(haku, lomake, hakemusMuutos, storedApplication, personOid)
      } ("Error validating application: " + hakemusMuutos.oid)
    }

    def validateAndFindQuestions(haku: Haku, lomake: Lomake, newHakemus: HakemusLike, storedApplication: Application, personOid: String)(implicit lang: Language.Language): HakemusInfo = {
      withErrorLogging {
        if (storedApplication.getPersonOid != personOid) throw new IllegalArgumentException("personId mismatch")
        val updatedApplication = update(newHakemus, lomake, storedApplication)
        val validationErrors: List[ValidationError] = validateHakutoiveetAndAnswers(updatedApplication, storedApplication, lomake) ++
          errorsForEditingInactiveHakuToive(updatedApplication, storedApplication, haku)

        val questions = AddedQuestionFinder.findQuestions(lomake)(storedApplication, newHakemus)
        val hakutoiveet = HakutoiveetConverter.convertFromAnswers(updatedApplication.getAnswers.toMap.mapValues(_.toMap))
        val hakuajat = hakutoiveet.map(hakutoiveData => hakutoiveData.get("Koulutus-id").map { koulutusId =>
          tarjontaService.hakukohde(koulutusId).getOrElse(Hakukohde(koulutusId, None, None))
        }.getOrElse(Hakukohde("", None, None)))

        HakemusInfo(hakemusConverter.convertToHakemus(lomake, haku, updatedApplication), validationErrors, questions, hakuajat)
      } ("Error validating application: " + newHakemus.oid)
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

    private def update(hakemusMuutos: HakemusLike, lomake: Lomake, application: Application)(implicit lang: Language.Language): Application = {
      ApplicationUpdater.update(lomake, application.clone(), hakemusMuutos) // application is mutated
    }

    private def validateAndConvertErrors(application: Application, appSystem: Lomake)(implicit lang: Language.Language) = {
      val result = validator.validate(convertToValidationInput(appSystem, application))
      convertoToValidationErrors(result)
    }

    private def errorsForEditingInactiveHakuToive(updatedApplication: Application, storedApplication: Application, haku: Haku)(implicit lang: Language.Language): List[ValidationError] = {
      val oldHakuToiveet = HakutoiveetConverter.convertFromAnswers(storedApplication.getAnswers.toMap.mapValues(_.toMap))
      val newHakuToiveet = HakutoiveetConverter.convertFromAnswers(updatedApplication.getAnswers.toMap.mapValues(_.toMap))
      val oldInactiveHakuToiveet: List[Hakukohde] = tarjontaService.inactiveHakuToiveet(oldHakuToiveet, haku)
      val newInactiveHakuToiveet: List[Hakukohde] = tarjontaService.inactiveHakuToiveet(newHakuToiveet, haku)
      val newHakutoiveetWithIndex = newHakuToiveet.zipWithIndex

      val addedInActiveHakutoiveet = newInactiveHakuToiveet.filter(!oldInactiveHakuToiveet.contains(_))

      val errorsForAdded = (addedInActiveHakutoiveet.flatMap { hakukohde =>
        newHakutoiveetWithIndex.find { case (hakutoive: HakutoiveData, index: Int) =>
          hakutoive.get("Koulutus-id").map { _ == hakukohde.oid }.getOrElse(false)
        }.map(_._2)
      }.map((index) => new ValidationError("preference"+(index+1) + "-Koulutus", Translations.getTranslation("error", "applicationPeriodNotActive")))
      )

      val errorsForOtherModifications =
        if (newInactiveHakuToiveet != oldInactiveHakuToiveet)
          List(new ValidationError("koulutus-id", Translations.getTranslation("error", "applicationPeriodNotActive")))
        else
          List.empty

      if (errorsForAdded.size != 0)
        errorsForAdded
      else
        errorsForOtherModifications
    }

    private def errorsForUnknownAnswers(lomake: Lomake, hakemusMuutos: HakemusMuutos)(implicit lang: Language.Language): List[ValidationError] = {
      val application = hakemusRepository.findStoredApplicationByOid(hakemusMuutos.oid)
      val allAnswers: Answers = ApplicationUpdater.getAllAnswersForApplication(lomake, application, hakemusMuutos)
      val acceptedAnswerIds: Seq[AnswerId] = AddedQuestionFinder.findAddedQuestions(lomake, allAnswers, Hakemus.emptyAnswers).flatMap(_.answerIds).toList

      val flatAnswers: List[(String, String, String)] = hakemusMuutos.answers.toList.flatMap {
        case (phaseId, groupAnswers) =>
          groupAnswers.toList.map { case (questionId, answer) =>
            (phaseId, questionId, answer)
          }
      }
      val unknownAnswers: List[(String, String, String)] = flatAnswers
        .filterNot {
          case (OppijaConstants.PHASE_PERSONAL, questionId, _) if List(OppijaConstants.ELEMENT_ID_FIN_ADDRESS, OppijaConstants.ELEMENT_ID_FIN_POSTAL_NUMBER, OppijaConstants.ELEMENT_ID_EMAIL, OppijaConstants.ELEMENT_ID_COUNTRY_OF_RESIDENCY).contains(questionId) || questionId.startsWith(OppijaConstants.ELEMENT_ID_PREFIX_PHONENUMBER) => true
          case (phaseId, questionId, _) => acceptedAnswerIds.contains(AnswerId(phaseId, questionId))
        }
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

