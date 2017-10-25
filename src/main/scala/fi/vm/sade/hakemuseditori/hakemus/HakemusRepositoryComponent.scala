package fi.vm.sade.hakemuseditori.hakemus

import java.util.Date

import fi.vm.sade.hakemuseditori.SendMailComponent
import fi.vm.sade.hakemuseditori.auditlog._
import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.hakemus.ImmutableLegacyApplicationWrapper.{LegacyApplicationAnswers, wrap}
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.{Valintatulos, Answers}
import fi.vm.sade.hakemuseditori.hakemus.domain._
import fi.vm.sade.hakemuseditori.hakumaksu.HakumaksuComponent
import fi.vm.sade.hakemuseditori.lomake.LomakeRepositoryComponent
import fi.vm.sade.hakemuseditori.lomake.domain.Lomake
import fi.vm.sade.hakemuseditori.ohjausparametrit.OhjausparametritComponent
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaComponent
import fi.vm.sade.hakemuseditori.tarjonta.domain.Haku
import fi.vm.sade.hakemuseditori.user.User
import fi.vm.sade.hakemuseditori.valintatulokset.ValintatulosServiceComponent
import fi.vm.sade.hakemuseditori.viestintapalvelu.TuloskirjeComponent
import fi.vm.sade.haku.oppija.hakemus.aspect.ApplicationDiffUtil
import fi.vm.sade.haku.oppija.hakemus.domain.{Application, ApplicationNote}
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationPeriod
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.utils.Timer._
import fi.vm.sade.utils.slf4j.Logging
import org.joda.time.LocalDateTime
import org.json4s.DefaultFormats

import scala.util.{Failure, Success, Try}

trait HakemusRepositoryComponent {
  this: LomakeRepositoryComponent with ApplicationValidatorComponent with HakemusConverterComponent
    with SpringContextComponent with AuditLoggerComponent with TarjontaComponent with OhjausparametritComponent
    with TuloskirjeComponent
    with ValintatulosServiceComponent with HakumaksuComponent with SendMailComponent =>

  import scala.collection.JavaConversions._

  val hakemusRepository = new HakemusFinder
  val applicationRepository = new ApplicationFinder
  val hakemusUpdater = new HakemusUpdater

  private val dao = springContext.applicationDAO

  class HakemusUpdater extends Logging {
    private val applicationService = springContext.applicationService

    def updateHakemus(lomake: Lomake, haku: Haku, hakemus: HakemusMuutos, user: User)(implicit lang: Language.Language): Try[Hakemus] = {
      val applicationQuery: Application = new Application().setOid(hakemus.oid)
      for {
        applicationJavaObject <- timed("Application fetch DAO", 1000) {dao.find(applicationQuery).toList.headOption} match {
          case Some(a) => Success(a)
          case None => Failure(new IllegalArgumentException("Application not found"))
        }
        originalApplication = wrap(applicationJavaObject)
        updatedAnswers = AnswerHelper.getUpdatedAnswersForApplication(lomake, originalApplication, hakemus)
        checkedAnswers <- Try.apply { checkPermissions(lomake, originalApplication, updatedAnswers, user) }
      } yield {
        mutateApplicationJavaObject(lomake, applicationJavaObject, checkedAnswers, haku.applicationPeriods.map(_.toApplicationPeriod), user) // <- the only point of actual mutation
        timed("Application update DAO", 1000){
          dao.update(applicationQuery, applicationJavaObject)
        }
        sendMailService.sendModifiedEmail(applicationJavaObject)

        val auditEvent = UpdateHakemus(user, hakemus.oid, haku.oid, originalApplication.answers, checkedAnswers)
        auditLogger.logDiff(auditEvent, auditEvent.getAnswerDiff)
        hakemusConverter.convertToHakemus(None, Some(lomake), haku, wrap(applicationJavaObject))
      }
    }

    private def checkPermissions(lomake: Lomake, originalApplication: ImmutableLegacyApplicationWrapper, newAnswers: Answers, user: User)(implicit lang: Language.Language) = {
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

      user.checkAccessToUserData(originalApplication.personOid)

      newAnswers
    }

    private def isActiveHakuPeriod(lomake: Lomake)(implicit lang: Language.Language) = {
      val applicationPeriods = lomakeRepository.applicationPeriodsByOid(lomake.oid)
      applicationPeriods.exists(_.active)
    }

    private def checkOnlyContactInfoChanges(lomake: Lomake, originalApplication: ImmutableLegacyApplicationWrapper, newAnswers: ImmutableLegacyApplicationWrapper.LegacyApplicationAnswers) {
      def isContactInformationChange(key: String): Boolean = {
        List(OppijaConstants.ELEMENT_ID_FIN_ADDRESS, OppijaConstants.ELEMENT_ID_EMAIL, OppijaConstants.ELEMENT_ID_EMAIL_DOUBLE, OppijaConstants.ELEMENT_ID_FIN_POSTAL_NUMBER).contains(key) ||
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

    private def mutateApplicationJavaObject(lomake: Lomake, application: Application, updatedAnswers: LegacyApplicationAnswers, applicationPeriods: List[ApplicationPeriod], user: User)(implicit lang: Language.Language) {
      val originalApplication = application.clone()
      application.setUpdated(new Date())
      updatedAnswers.foreach { case (phaseId, phaseAnswers) =>
        application.setVaiheenVastauksetAndSetPhaseId(phaseId, phaseAnswers)
      }
      timed("ApplicationService: update preference based data", 1000){
        applicationService.updatePreferenceBasedData(application)
      }
      timed("ApplicationService: update authorization Meta", 1000){
        applicationService.updateAuthorizationMeta(application)
      }
      timed("ApplicationService: update automatic eligibilities", 1000) {
        applicationService.updateAutomaticEligibilities(application)
      }

      val applicationWithPayment = if (lomakeRepository.isMaksumuuriKaytossa(application.getApplicationSystemId)) {
        // mutates application and sends email
        hakumaksuService.processPayment(application, applicationPeriods)
      } else {
        application
      }

      updateChangeHistory(applicationWithPayment, originalApplication, user)
    }

    private def updateChangeHistory(application: Application, originalApplication: Application, user: User) {
      val changes = ApplicationDiffUtil.addHistoryBasedOnChangedAnswers(application, originalApplication, user.toString, "Muokkaus " + auditContext.systemName + " -palvelussa")

      val changedKeys: Set[String] = changes.toList.flatMap(_.toMap.get("field")).toSet
      val changedPhases: List[String] = application.getAnswers.toMap.toList.filter { case (vaihe, vastaukset) =>
        vastaukset.toMap.keys.exists(changedKeys.contains)
      }.map(_._1).map("'" + _ + "'")

      val noteText = "Hakija päivittänyt " + (if (changedPhases.size == 1) { "vaihetta" } else { "vaiheita" }) + " " + changedPhases.mkString(", ")

      application.addNote(new ApplicationNote(noteText, new Date(), user.oid))
    }
  }

  class ApplicationFinder {
    def findStoredApplicationByOid(oid: String): Option[ImmutableLegacyApplicationWrapper] = {
      findStoredApplication(new Application().setOid(oid)).headOption.map(wrap)
    }

    def exists(personOid: String, hakemusOid: String) = {
      findStoredApplicationByPersonAndOid(personOid, hakemusOid).isDefined
    }

    def findStoredApplicationByPersonAndOid(personOid: String, oid: String) = {
      findStoredApplicationByOid(oid).filter(application => personOid.equals(application.personOid))
    }

    def applicationsByPersonOid(personOid: String): Iterable[ImmutableLegacyApplicationWrapper] = {
      findStoredApplication(new Application().setPersonOid(personOid)).map(wrap)
    }

    private def findStoredApplication(query: Application): Iterable[Application] = {
      timed("Application fetch DAO", 1000)(dao.find(query).toVector)
    }
  }

  class HakemusFinder {
    private val applicationValidator: ApplicationValidator = newApplicationValidator

    def fetchHakemukset(personOid: String)(implicit lang: Language.Language): List[HakemusInfo] = {
      fetchHakemukset(new Application().setPersonOid(personOid), _ => true)
    }

    def getHakemus(hakemusOid: String, fetchTulosForHakemus: ImmutableLegacyApplicationWrapper => Boolean = _ => true)
                  (implicit lang: Language): Option[HakemusInfo] = {
      fetchHakemukset(new Application().setOid(hakemusOid), fetchTulosForHakemus).headOption
    }
    private def fetchHakemukset(query: Application,
                                fetchTulosForHakemus: ImmutableLegacyApplicationWrapper => Boolean)
                               (implicit lang: Language): List[HakemusInfo] = {
      timed("Application fetch", 1000){
        val legacyApplications: List[ImmutableLegacyApplicationWrapper] = timed("Application fetch DAO", 1000){
          dao.find(query).toList
        }.map(ImmutableLegacyApplicationWrapper.wrap)
        legacyApplications.filter {
          application => {
            !application.state.equals("PASSIVE")
          }
        }.flatMap(application => {
          val (lomakeOption, hakuOption) = timed("LomakeRepository get lomake", 1000) {
            lomakeRepository.lomakeAndHakuByApplication(application)
          }
          for {
            haku <- hakuOption
          } yield {
            val fetchTulos = fetchTulosForHakemus(application)
            val (valintatulos, tulosOk) = if (fetchTulos) {
              timed("fetchHakemukset -> fetchValintatulos", 100) { fetchValintatulos(application, haku, lomakeOption) }
            } else {
              (None, true)
            }
            val letterForHaku = tuloskirjeService.getTuloskirjeInfo(haku.oid, application.oid)
            val hakemus = timed("fetchHakemukset -> hakemusConverter.convertToHakemus", 100) { hakemusConverter.convertToHakemus(letterForHaku, lomakeOption, haku, application, valintatulos) }
            timed("fetchHakemukset -> auditLogger.log", 100) { auditLogger.log(ShowHakemus(application.personOid, hakemus.oid, haku.oid)) }

            lomakeOption match {
              case Some(lomake) if haku.applicationPeriods.exists(_.active) =>
                timed("fetchHakemukset -> applicationValidator.validateAndFindQuestions", 100) { applicationValidator.validateAndFindQuestions(haku, lomake, withNoPreferenceSpesificAnswers(hakemus), application) match {
                    case (app, errors, questions) => HakemusInfo(hakemusConverter.convertToHakemus(letterForHaku, Some(lomake), haku, app, valintatulos), errors, questions, tulosOk, None, "HakuApp", "")
                  }
                }
              case _ =>
                HakemusInfo(hakemus, List(), List(), tulosOk, None, "HakuApp", "")
            }
          }
        })
      }
    }

    private def fetchValintatulos(application: ImmutableLegacyApplicationWrapper, haku: Haku, lomake: Option[Lomake])(implicit lang: Language) = {
      Try(valintatulosService.getValintatulos(application.oid, haku.oid)) match {
        case Success(t) => (t, true)
        case Failure(e) => (None, false)
      }
    }

    private def withNoPreferenceSpesificAnswers(hakemus: Hakemus): HakemusLike = {
      hakemus.toHakemusMuutos.copy(answers = hakemus.answers.filterKeys(!_.equals(HakutoiveetConverter.hakutoiveetPhase)))
    }
  }
}
