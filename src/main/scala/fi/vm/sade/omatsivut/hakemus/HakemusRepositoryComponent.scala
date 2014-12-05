package fi.vm.sade.omatsivut.hakemus

import java.util.Date

import fi.vm.sade.haku.oppija.hakemus.aspect.ApplicationDiffUtil
import fi.vm.sade.haku.oppija.hakemus.domain.{Application, ApplicationNote}
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.auditlog._
import fi.vm.sade.omatsivut.config.SpringContextComponent
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.domain.Language.Language
import fi.vm.sade.omatsivut.hakemus.ImmutableLegacyApplicationWrapper.wrap
import fi.vm.sade.omatsivut.hakemus.domain._
import fi.vm.sade.omatsivut.lomake.LomakeRepositoryComponent
import fi.vm.sade.omatsivut.lomake.domain.Lomake
import fi.vm.sade.omatsivut.ohjausparametrit.OhjausparametritComponent
import fi.vm.sade.omatsivut.tarjonta.TarjontaComponent
import fi.vm.sade.omatsivut.tarjonta.domain.Haku
import fi.vm.sade.omatsivut.util.Timer._
import fi.vm.sade.omatsivut.valintatulokset.ValintatulosServiceComponent
import org.joda.time.LocalDateTime

import scala.util.{Failure, Success, Try}

trait HakemusRepositoryComponent {
  this: LomakeRepositoryComponent with ApplicationValidatorComponent with HakemusConverterComponent with SpringContextComponent with AuditLoggerComponent with TarjontaComponent with OhjausparametritComponent with ValintatulosServiceComponent =>

  val hakemusRepository: HakemusRepository
  val applicationRepository: ApplicationRepository

  class RemoteHakemusRepository extends HakemusRepository with ApplicationRepository {
    // TODO: don't implement two interfaces here, split!

    import scala.collection.JavaConversions._
    private val dao = springContext.applicationDAO
    private val applicationService = springContext.applicationService
    private val applicationValidator: ApplicationValidator = newApplicationValidator

    private def canUpdate(lomake: Lomake, originalApplication: Application, updatedApplication: Application, userOid: String)(implicit lang: Language.Language): Boolean = {
      val stateUpdateable = originalApplication.getState == Application.State.ACTIVE || originalApplication.getState == Application.State.INCOMPLETE
      val inPostProcessing = !(originalApplication.getRedoPostProcess == Application.PostProcessingState.DONE || originalApplication.getRedoPostProcess() == null)
      (isActiveHakuPeriod(lomake) || hasOnlyContactInfoChangesAndApplicationRoundHasNotEnded(lomake, originalApplication, updatedApplication)) &&
      stateUpdateable &&
      !inPostProcessing &&
      userOid == originalApplication.getPersonOid
    }

    private def isActiveHakuPeriod(lomake: Lomake)(implicit lang: Language.Language) = {
      val applicationPeriods = lomakeRepository.applicationPeriodsByOid(lomake.oid)
      applicationPeriods.exists(_.active)
    }

    private def hasOnlyContactInfoChangesAndApplicationRoundHasNotEnded(lomake: Lomake, originalApplication: Application, updatedApplication: Application): Boolean = {
      val oldAnswers = originalApplication.getVastauksetMerged
      val newAnswers = updatedApplication.getVastauksetMerged
      val allKeys = oldAnswers.keySet() ++ newAnswers.keySet()
      new LocalDateTime().isBefore(ohjausparametritService.haunAikataulu(lomake.oid).flatMap(_.hakukierrosPaattyy).map(new LocalDateTime(_ : Long)).getOrElse(new LocalDateTime().plusYears(100))) && allKeys.filter(
        key => {
          val oldValue = oldAnswers.getOrElse(key, "")
          val newValue = newAnswers.getOrElse(key, "")
          if(oldValue.equals(newValue)) {
            false
          }
          else {
            !isContactInformationChange(key)
          }
        }
      ).isEmpty
    }

    private def isContactInformationChange(key: String): Boolean = {
      List(OppijaConstants.ELEMENT_ID_FIN_ADDRESS, OppijaConstants.ELEMENT_ID_EMAIL, OppijaConstants.ELEMENT_ID_FIN_POSTAL_NUMBER).contains(key) ||
      key.startsWith(OppijaConstants.ELEMENT_ID_PREFIX_PHONENUMBER)
    }

    override def updateHakemus(lomake: Lomake, haku: Haku)(hakemus: HakemusMuutos, userOid: String)(implicit lang: Language.Language): Option[Hakemus] = {
      val applicationQuery: Application = new Application().setOid(hakemus.oid)
      val applicationJavaObject: Option[Application] = timed(1000, "Application fetch DAO"){
        dao.find(applicationQuery).toList.headOption
      }

      timed(1000, "Application update"){
        applicationJavaObject.map(updateApplication(lomake, _, hakemus, userOid)).filter { case (originalApplication: Application, application: Application) =>
          canUpdate(lomake, originalApplication, application, userOid)
        }.map { case (originalApplication, application) =>
          timed(1000, "Application update DAO"){
            dao.update(applicationQuery, application)
          }
          auditLogger.log(UpdateHakemus(userOid, hakemus.oid, haku.oid, originalApplication.getAnswers.toMap.mapValues(_.toMap), application.getAnswers.toMap.mapValues(_.toMap)))
          hakemusConverter.convertToHakemus(lomake, haku, wrap(application))
        }
      }
    }

    private def updateApplication(lomake: Lomake, application: Application, hakemus: HakemusMuutos, userOid: String)(implicit lang: Language.Language): (Application, Application) = {
      val originalApplication = application.clone()
      application.setUpdated(new Date())
      val updatedAnswers = AnswerHelper.getUpdatedAnswersForApplication(lomake, wrap(application), hakemus)
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

      (originalApplication, application)
    }

    private def updateChangeHistory(application: Application, originalApplication: Application, userOid: String) {
      val changes = ApplicationDiffUtil.addHistoryBasedOnChangedAnswers(application, originalApplication, userOid, "Muokkaus Omat Sivut -palvelussa");

      val changedKeys: Set[String] = changes.toList.flatMap(_.toMap.get("field")).toSet
      val changedPhases: List[String] = application.getAnswers.toMap.toList.filter { case (vaihe, vastaukset) =>
        vastaukset.toMap.keys.exists(changedKeys.contains(_))
      }.map(_._1)
      val noteText = changedPhases.map("Hakija päivittänyt vaihetta '" + _ + "'").mkString("\n")

      application.addNote(new ApplicationNote(noteText, new Date(), userOid))
    }

    override def findStoredApplicationByOid(oid: String): Option[ImmutableLegacyApplicationWrapper] = {
      findStoredApplication(new Application().setOid(oid))
    }

    override def findStoredApplicationByPersonAndOid(personOid: String, oid: String) = {
      findStoredApplication(new Application().setOid(oid).setPersonOid(personOid))
    }

    private def findStoredApplication(query: Application) = {
      val applications = timed(1000, "Application fetch DAO"){
        dao.find(query).toList
      }
      applications.headOption.map(wrap)
    }


    override def fetchHakemukset(personOid: String)(implicit lang: Language.Language) = {
      fetchHakemukset(new Application().setPersonOid(personOid))
    }

    override def getHakemus(personOid: String, hakemusOid: String)(implicit lang: Language) = {
      fetchHakemukset(new Application().setPersonOid(personOid).setOid(hakemusOid)).headOption
    }

    private def fetchHakemukset(query: Application)(implicit lang: Language) = {
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

    override def exists(personOid: String, hakuOid: String, hakemusOid: String) = {
      dao.find(new Application().setPersonOid(personOid).setOid(hakemusOid).setApplicationSystemId(hakuOid)).size() == 1
    }
  }
}
