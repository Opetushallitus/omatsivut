package fi.vm.sade.hakemuseditori.hakemus

import java.util.Date

import fi.vm.sade.haku.oppija.hakemus.aspect.ApplicationDiffUtil
import fi.vm.sade.haku.oppija.hakemus.domain.{Application, ApplicationNote}
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.hakemuseditori.auditlog._
import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.hakemus.ImmutableLegacyApplicationWrapper.{LegacyApplicationAnswers, wrap}
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.Answers
import fi.vm.sade.hakemuseditori.hakemus.domain._
import fi.vm.sade.hakemuseditori.lomake.LomakeRepositoryComponent
import fi.vm.sade.hakemuseditori.lomake.domain.Lomake
import fi.vm.sade.hakemuseditori.ohjausparametrit.OhjausparametritComponent
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaComponent
import fi.vm.sade.hakemuseditori.tarjonta.domain.Haku
import fi.vm.sade.utils.slf4j.Logging
import fi.vm.sade.utils.Timer._
import fi.vm.sade.hakemuseditori.valintatulokset.ValintatulosServiceComponent
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

    def updateHakemus(lomake: Lomake, haku: Haku, hakemus: HakemusMuutos, userOid: String)(implicit lang: Language.Language): Try[Hakemus] = {
      val applicationQuery: Application = new Application().setOid(hakemus.oid)
      for {
        applicationJavaObject <- timed("Application fetch DAO", 1000) {dao.find(applicationQuery).toList.headOption} match {
          case Some(a) => Success(a)
          case None => Failure(new IllegalArgumentException("Application not found"))
        }
        originalApplication = wrap(applicationJavaObject)
        updatedAnswers = AnswerHelper.getUpdatedAnswersForApplication(lomake, originalApplication, hakemus)
        checkedAnswers <- Try.apply { checkPermissions(lomake, originalApplication, updatedAnswers, userOid) }
      } yield {
        mutateApplicationJavaObject(lomake, applicationJavaObject, checkedAnswers, userOid) // <- the only point of actual mutation
        timed("Application update DAO", 1000){
          dao.update(applicationQuery, applicationJavaObject)
        }
        auditLogger.log(UpdateHakemus(userOid, hakemus.oid, haku.oid, originalApplication.answers, checkedAnswers))
        hakemusConverter.convertToHakemus(Some(lomake), haku, wrap(applicationJavaObject))
      }
    }

    private def checkPermissions(lomake: Lomake, originalApplication: ImmutableLegacyApplicationWrapper, newAnswers: Answers, userOid: String)(implicit lang: Language.Language) = {
      if (!(originalApplication.state == "ACTIVE" || originalApplication.state == "INCOMPLETE")) {
        throw new IllegalStateException("Not updateable state: " + originalApplication.state)
      }

      if (!isActiveHakuPeriod(lomake)) {
        checkOnlyContactInfoChanges(lomake, originalApplication, newAnswers)

        val hakukierrosPäättyy: LocalDateTime = ohjausparametritService.haunAikataulu(lomake.oid).flatMap(_.hakukierrosPaattyy).map(new LocalDateTime(_: Long)).getOrElse(new LocalDateTime().plusYears(100))

        if (!new LocalDateTime().isBefore(hakukierrosPäättyy)) {
          throw new IllegalArgumentException("Hakukierros päättynyt " + hakukierrosPäättyy)
        }
      }

      if (originalApplication.isPostProcessing) {
        throw new IllegalStateException("In post-processing")
      }

      if (userOid != originalApplication.personOid) {
        throw new IllegalArgumentException("Person oid mismatch")
      }

      newAnswers
    }

    private def isActiveHakuPeriod(lomake: Lomake)(implicit lang: Language.Language) = {
      val applicationPeriods = lomakeRepository.applicationPeriodsByOid(lomake.oid)
      applicationPeriods.exists(_.active)
    }

    private def checkOnlyContactInfoChanges(lomake: Lomake, originalApplication: ImmutableLegacyApplicationWrapper, newAnswers: ImmutableLegacyApplicationWrapper.LegacyApplicationAnswers) {
      def isContactInformationChange(key: String): Boolean = {
        List(OppijaConstants.ELEMENT_ID_FIN_ADDRESS, OppijaConstants.ELEMENT_ID_EMAIL, OppijaConstants.ELEMENT_ID_FIN_POSTAL_NUMBER).contains(key) ||
          key.startsWith(OppijaConstants.ELEMENT_ID_PREFIX_PHONENUMBER)
      }

      val oldAnswersFlattened = originalApplication.flatAnswers
      val newAnswersFlattened = FlatAnswers.flatten(newAnswers)

      newAnswersFlattened.keys.foreach(
        key => {
          val oldValue = oldAnswersFlattened.getOrElse(key, "")
          val newValue = newAnswersFlattened.getOrElse(key, "")
          val changed = oldValue != newValue && !isContactInformationChange(key)
          if (changed) {
            throw new IllegalArgumentException("Attempt to change a non-contact information value " + key + "=" + oldValue + "->" + newValue + " for application " + originalApplication.oid)
          }
          changed
        }
      )
    }

    private def mutateApplicationJavaObject(lomake: Lomake, application: Application, updatedAnswers: LegacyApplicationAnswers, userOid: String)(implicit lang: Language.Language) {
      val originalApplication = application.clone()
      application.setUpdated(new Date())
      updatedAnswers.foreach { case (phaseId, phaseAnswers) =>
        application.addVaiheenVastaukset(phaseId, phaseAnswers)
      }
      timed("ApplicationService: update preference based data", 1000){
        applicationService.updatePreferenceBasedData(application)
      }
      timed("ApplicationService: update authorization Meta", 1000){
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
      val applications = timed("Application fetch DAO", 1000){
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
      timed("Application fetch", 1000){
        val legacyApplications: List[ImmutableLegacyApplicationWrapper] = timed("Application fetch DAO", 1000){
          dao.find(query).toList
        }.map(ImmutableLegacyApplicationWrapper.wrap)
        legacyApplications.filter{
          application => {
            !application.state.equals("PASSIVE")
          }
        }.map(application => {
          val (lomakeOption, hakuOption) = timed("LomakeRepository get lomake", 1000){
            lomakeRepository.lomakeAndHakuByApplication(application)
          }
          for {
            haku <- hakuOption
          } yield {
            val (valintatulos, tulosOk) = fetchValintatulos(application, haku)
            val hakemus = hakemusConverter.convertToHakemus(lomakeOption, haku, application, valintatulos)
            auditLogger.log(ShowHakemus(application.personOid, hakemus.oid, haku.oid))

            lomakeOption match {
              case Some(lomake) if haku.applicationPeriods.exists(_.active) =>
                applicationValidator.validateAndFindQuestions(haku, lomake, withNoPreferenceSpesificAnswers(hakemus), application) match {
                  case (app, errors, questions) => HakemusInfo(hakemusConverter.convertToHakemus(Some(lomake), haku, app, valintatulos), errors, questions, tulosOk)
                }
              case _ =>
                HakemusInfo(hakemus, List(), List(), tulosOk)
            }
          }
        }).flatten.toList.sortBy[Option[Long]](_.hakemus.received).reverse
      }
    }

    private def fetchValintatulos(application: ImmutableLegacyApplicationWrapper, haku: Haku)(implicit lang: Language) = {
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
