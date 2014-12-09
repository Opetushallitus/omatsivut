package fi.vm.sade.omatsivut.hakemus

import java.util.Date

import fi.vm.sade.haku.oppija.hakemus.aspect.ApplicationDiffUtil
import fi.vm.sade.haku.oppija.hakemus.domain.{Application, ApplicationNote}
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.auditlog._
import fi.vm.sade.omatsivut.config.SpringContextComponent
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.domain.Language.Language
import fi.vm.sade.omatsivut.hakemus.FlatAnswers.FlatAnswers
import fi.vm.sade.omatsivut.hakemus.ImmutableLegacyApplicationWrapper.{LegacyApplicationAnswers, wrap}
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus.Answers
import fi.vm.sade.omatsivut.hakemus.domain._
import fi.vm.sade.omatsivut.lomake.LomakeRepositoryComponent
import fi.vm.sade.omatsivut.lomake.domain.Lomake
import fi.vm.sade.omatsivut.ohjausparametrit.OhjausparametritComponent
import fi.vm.sade.omatsivut.tarjonta.TarjontaComponent
import fi.vm.sade.omatsivut.tarjonta.domain.Haku
import fi.vm.sade.omatsivut.util.Logging
import fi.vm.sade.omatsivut.util.Timer._
import fi.vm.sade.omatsivut.valintatulokset.ValintatulosServiceComponent
import org.joda.time.LocalDateTime

import scala.util.{Failure, Success, Try}

trait HakemusRepositoryComponent {
  this: LomakeRepositoryComponent with ApplicationValidatorComponent with HakemusConverterComponent with SpringContextComponent with AuditLoggerComponent with TarjontaComponent with OhjausparametritComponent with ValintatulosServiceComponent =>

  import scala.collection.JavaConversions._

  val hakemusRepository = new HakemusFinder
  val applicationRepository = new ApplicationFinder
  val hakemusUpdater = new HakemusUpdater

  private val dao = springContext.applicationDAO

  class HakemusUpdater extends Logging {
    private val applicationService = springContext.applicationService

    def updateHakemus(lomake: Lomake, haku: Haku, hakemus: HakemusMuutos, userOid: String)(implicit lang: Language.Language): Option[Hakemus] = {
      val applicationQuery: Application = new Application().setOid(hakemus.oid)
      for {
        applicationJavaObject <- timed(1000, "Application fetch DAO") {dao.find(applicationQuery).toList.headOption}
        originalApplication = wrap(applicationJavaObject)
        updatedAnswers = AnswerHelper.getUpdatedAnswersForApplication(lomake, originalApplication, hakemus)
        if canUpdate(lomake, originalApplication, updatedAnswers, userOid)
      } yield {
        mutateApplicationJavaObject(lomake, applicationJavaObject, updatedAnswers, userOid) // <- the only point of actual mutation
        timed(1000, "Application update DAO"){
          dao.update(applicationQuery, applicationJavaObject)
        }
        auditLogger.log(UpdateHakemus(userOid, hakemus.oid, haku.oid, originalApplication.answers, updatedAnswers))
        hakemusConverter.convertToHakemus(lomake, haku, wrap(applicationJavaObject))
      }
    }

    private def canUpdate(lomake: Lomake, originalApplication: ImmutableLegacyApplicationWrapper, newAnswers: Answers, userOid: String)(implicit lang: Language.Language): Boolean = {
      val stateUpdateable = originalApplication.state == "ACTIVE" || originalApplication.state == "INCOMPLETE"
      val inPostProcessing = originalApplication.isPostProcessing

      (isActiveHakuPeriod(lomake) || hasOnlyContactInfoChangesAndApplicationRoundHasNotEnded(lomake, originalApplication, newAnswers)) &&
        stateUpdateable &&
        !inPostProcessing &&
        userOid == originalApplication.personOid
    }

    private def isActiveHakuPeriod(lomake: Lomake)(implicit lang: Language.Language) = {
      val applicationPeriods = lomakeRepository.applicationPeriodsByOid(lomake.oid)
      applicationPeriods.exists(_.active)
    }

    private def hasOnlyContactInfoChangesAndApplicationRoundHasNotEnded(lomake: Lomake, originalApplication: ImmutableLegacyApplicationWrapper, newAnswers: ImmutableLegacyApplicationWrapper.LegacyApplicationAnswers): Boolean = {
      def isContactInformationChange(key: String): Boolean = {
        List(OppijaConstants.ELEMENT_ID_FIN_ADDRESS, OppijaConstants.ELEMENT_ID_EMAIL, OppijaConstants.ELEMENT_ID_FIN_POSTAL_NUMBER).contains(key) ||
          key.startsWith(OppijaConstants.ELEMENT_ID_PREFIX_PHONENUMBER)
      }

      val oldAnswersFlattened = originalApplication.flatAnswers
      val newAnswersFlattened = FlatAnswers.flatten(newAnswers)

      val hakukierrosPäättyy: LocalDateTime = ohjausparametritService.haunAikataulu(lomake.oid).flatMap(_.hakukierrosPaattyy).map(new LocalDateTime(_: Long)).getOrElse(new LocalDateTime().plusYears(100))

      new LocalDateTime().isBefore(hakukierrosPäättyy) && newAnswersFlattened.keys.find(
        key => {
          val oldValue = oldAnswersFlattened.getOrElse(key, "")
          val newValue = newAnswersFlattened.getOrElse(key, "")
          val changed = oldValue != newValue && !isContactInformationChange(key)
          if (changed) {
            logger.warn("Attempt to change a non-contact information value " + key + " for application " + originalApplication.oid)
          }
          changed
        }
      ).isEmpty
    }

    private def mutateApplicationJavaObject(lomake: Lomake, application: Application, updatedAnswers: LegacyApplicationAnswers, userOid: String)(implicit lang: Language.Language) {
      val originalApplication = application.clone()
      application.setUpdated(new Date())
      updatedAnswers.foreach { case (phaseId, phaseAnswers) =>
        application.addVaiheenVastaukset(phaseId, phaseAnswers)
      }
      timed(1000, "ApplicationService: update preference based data"){
        applicationService.updatePreferenceBasedData(application)
      }
      timed(1000, "ApplicationService: update authorization Meta"){
        applicationService.updateAuthorizationMeta(application)
      }
      updateChangeHistory(application, originalApplication, userOid)
    }

    private def updateChangeHistory(application: Application, originalApplication: Application, userOid: String) {
      val changes = ApplicationDiffUtil.addHistoryBasedOnChangedAnswers(application, originalApplication, userOid, "Muokkaus Omat Sivut -palvelussa");

      val changedKeys: Set[String] = changes.toList.flatMap(_.toMap.get("field")).toSet
      val changedPhases: List[String] = application.getAnswers.toMap.toList.filter { case (vaihe, vastaukset) =>
        vastaukset.toMap.keys.exists(changedKeys.contains(_))
      }.map(_._1).map("'" + _ + "'")

      val noteText = "Hakija päivittänyt " + (if (changedPhases.size == 1) { "vaihetta" } else { "vaiheita" }) + " " + changedPhases.mkString(", ")

      application.addNote(new ApplicationNote(noteText, new Date(), userOid))
    }
  }

  class ApplicationFinder {
    def findStoredApplicationByOid(oid: String): Option[ImmutableLegacyApplicationWrapper] = {
      findStoredApplication(new Application().setOid(oid))
    }

    def findStoredApplicationByPersonAndOid(personOid: String, oid: String) = {
      findStoredApplication(new Application().setOid(oid).setPersonOid(personOid))
    }

    def findStoredApplication(query: Application) = {
      val applications = timed(1000, "Application fetch DAO"){
        dao.find(query).toList
      }
      applications.headOption.map(wrap)
    }
  }

  class HakemusFinder {
    private val applicationValidator: ApplicationValidator = newApplicationValidator

    def fetchHakemukset(personOid: String)(implicit lang: Language.Language): List[HakemusInfo] = {
      fetchHakemukset(new Application().setPersonOid(personOid))
    }

    def getHakemus(personOid: String, hakemusOid: String)(implicit lang: Language): Option[HakemusInfo] = {
      fetchHakemukset(new Application().setPersonOid(personOid).setOid(hakemusOid)).headOption
    }

    def exists(personOid: String, hakuOid: String, hakemusOid: String) = {
      dao.find(new Application().setPersonOid(personOid).setOid(hakemusOid).setApplicationSystemId(hakuOid)).size() == 1
    }

    private def fetchHakemukset(query: Application)(implicit lang: Language): List[HakemusInfo] = {
      timed(1000, "Application fetch"){
        val legacyApplications: List[ImmutableLegacyApplicationWrapper] = timed(1000, "Application fetch DAO"){
          dao.find(query).toList
        }.map(ImmutableLegacyApplicationWrapper.wrap)
        legacyApplications.filter{
          application => {
            !application.state.equals("PASSIVE")
          }
        }.map(application => {
          val (lomakeOption, hakuOption) = timed(1000, "LomakeRepository get lomake"){
            lomakeRepository.lomakeAndHakuByApplication(application)
          }
          for {
            haku <- hakuOption
            lomake <- lomakeOption
          } yield {
            val valintatulos = fetchValintatulos(application, haku)
            val hakemus = hakemusConverter.convertToHakemus(lomake, haku, application, valintatulos._1)
            auditLogger.log(ShowHakemus(application.personOid, hakemus.oid, haku.oid))

            if (haku.applicationPeriods.exists(_.active)) {
              applicationValidator.validateAndFindQuestions(haku, lomake, withNoPreferenceSpesificAnswers(hakemus), application) match {
                case (app, errors, questions) => HakemusInfo(hakemusConverter.convertToHakemus(lomake, haku, app, valintatulos._1), errors, questions, valintatulos._2)
              }
            }
            else {
              HakemusInfo(hakemus, List(), List(), valintatulos._2)
            }
          }
        }).flatten.toList.sortBy[Long](_.hakemus.received).reverse
      }
    }

    private def fetchValintatulos(application: ImmutableLegacyApplicationWrapper, haku: Haku) = {
      if (hakemusConverter.anyApplicationPeriodEnded(haku, application)) {
        Try(valintatulosService.getValintatulos(application.oid, haku.oid)) match {
          case Success(t) => (t, true)
          case Failure(e) => (None, false)
        }
      } else {
        (None, true)
      }
    }

    private def withNoPreferenceSpesificAnswers(hakemus: Hakemus): HakemusLike = {
      hakemus.toHakemusMuutos.copy(answers = hakemus.answers.filterKeys(!_.equals(HakutoiveetConverter.hakutoiveetPhase)))
    }
  }
}
