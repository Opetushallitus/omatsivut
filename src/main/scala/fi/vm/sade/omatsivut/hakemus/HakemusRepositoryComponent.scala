package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.omatsivut.auditlog._
import fi.vm.sade.omatsivut.config.SpringContextComponent
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.domain.Language.Language
import fi.vm.sade.omatsivut.hakemus.domain._
import fi.vm.sade.omatsivut.haku.{Lomake, HakuRepository, HakuRepositoryComponent}
import fi.vm.sade.omatsivut.tarjonta.{Haku, TarjontaComponent}
import fi.vm.sade.omatsivut.util.Timer.timed

trait HakemusRepositoryComponent {
  this: HakuRepositoryComponent with HakemusConverterComponent with SpringContextComponent with AuditLoggerComponent with TarjontaComponent =>

  val hakuRepository: HakuRepository

  class RemoteHakemusRepository extends HakemusRepository {
    import scala.collection.JavaConversions._
    private val dao = springContext.applicationDAO
    private val applicationService = springContext.applicationService

    private def canUpdate(lomake: Lomake, application: Application, userOid: String)(implicit lang: Language.Language): Boolean = {
      val applicationPeriods = hakuRepository.getApplicationPeriods(lomake.oid)
      val isActiveHakuPeriod = applicationPeriods.exists(_.active)
      val stateUpdateable = application.getState == Application.State.ACTIVE || application.getState == Application.State.INCOMPLETE
      val inPostProcessing = !(application.getRedoPostProcess() == Application.PostProcessingState.DONE || application.getRedoPostProcess() == null)
      isActiveHakuPeriod && stateUpdateable && !inPostProcessing && userOid == application.getPersonOid
    }

    override def updateHakemus(lomake: Lomake, haku: Haku)(hakemus: HakemusMuutos, userOid: String)(implicit lang: Language.Language): Option[Hakemus] = {
      val applicationQuery: Application = new Application().setOid(hakemus.oid)
      val applicationJavaObject: Option[Application] = timed(1000, "Application fetch DAO"){
        dao.find(applicationQuery).toList.headOption
      }

      timed(1000, "Application update"){
        applicationJavaObject.filter(application => canUpdate(lomake, application, userOid)).map { application =>
          val originalAnswers: Hakemus.Answers = application.getAnswers.toMap.mapValues(_.toMap)
          ApplicationUpdater.update(lomake)(application, hakemus)
          timed(1000, "ApplicationService: update preference based data"){
            applicationService.updatePreferenceBasedData(application)
          }
          timed(1000, "ApplicationService: update authorization Meta"){
            applicationService.updateAuthorizationMeta(application, false)
          }
          timed(1000, "Application update DAO"){
            dao.update(applicationQuery, application)
          }
          auditLogger.log(UpdateHakemus(userOid, hakemus.oid, originalAnswers, application.getAnswers.toMap.mapValues(_.toMap)))
          hakemusConverter.convertToHakemus(lomake, haku, application)
        }
      }
    }


    override def findStoredApplicationByOid(oid: String): Application = {
      val applications = timed(1000, "Application fetch DAO"){
        dao.find(new Application().setOid(oid)).toList
      }
      if (applications.size > 1) throw new RuntimeException("Too many applications for oid " + oid)
      if (applications.size == 0) throw new RuntimeException("Application not found for oid " + oid)
      val application = applications.head
      application
    }

    override def fetchHakemukset(personOid: String)(implicit lang: Language.Language): List[Hakemus] = {
      fetchHakemukset(new Application().setPersonOid(personOid))
    }

    override def getHakemus(personOid: String, hakemusOid: String)(implicit lang: Language) = {
      fetchHakemukset(new Application().setPersonOid(personOid).setOid(hakemusOid)).headOption
    }

    private def fetchHakemukset(query: Application)(implicit lang: Language) = {
      timed(1000, "Application fetch"){
        val applicationJavaObjects: List[Application] = timed(1000, "Application fetch DAO"){
          dao.find(query).toList
        }
        applicationJavaObjects.filter{
          application => {
            !application.getState.equals(Application.State.PASSIVE)
          }
        }.map(application => {
          val (applicationSystemOption, hakuOption) = timed(1000, "HakuRepository get haku"){
            hakuRepository.getHakuByApplication(application)
          }
          for {
            haku <- hakuOption
            lomake <- applicationSystemOption
          } yield {
            val hakemus = hakemusConverter.convertToHakemus(lomake, haku, application)
            auditLogger.log(ShowHakemus(application.getPersonOid, hakemus.oid))
            hakemus
          }
        }).flatten.toList.sortBy[Long](_.received).reverse
      }
    }

    override def exists(personOid: String, hakuOid: String, hakemusOid: String) = {
      dao.find(new Application().setPersonOid(personOid).setOid(hakemusOid).setApplicationSystemId(hakuOid)).size() == 1
    }
  }

}
