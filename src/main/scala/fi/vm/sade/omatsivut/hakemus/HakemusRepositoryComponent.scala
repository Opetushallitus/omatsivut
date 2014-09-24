package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.auditlog._
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.config.SpringContextComponent
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.domain.Language.Language
import fi.vm.sade.omatsivut.hakemus.domain._
import fi.vm.sade.omatsivut.haku.domain.Haku
import fi.vm.sade.omatsivut.haku.{HakuConverter, HakuRepository, HakuRepositoryComponent}
import fi.vm.sade.omatsivut.util.Timer
import fi.vm.sade.haku.oppija.hakemus.service.ApplicationService

trait HakemusRepositoryComponent {
  this: HakuRepositoryComponent with HakemusConverterComponent with SpringContextComponent with AuditLoggerComponent =>

  val hakuRepository: HakuRepository

  class RemoteHakemusRepository extends Timer with HakemusRepository {
    import scala.collection.JavaConversions._
    private val dao = springContext.applicationDAO
    private val applicationService = springContext.applicationService

    private def canUpdate(applicationSystem: ApplicationSystem, application: Application, userOid: String)(implicit lang: Language.Language): Boolean = {
      val applicationPeriods = hakuRepository.getApplicationPeriods(application, applicationSystem)
      val isActiveHakuPeriod = applicationPeriods.exists(_.active)
      val stateUpdateable = application.getState == Application.State.ACTIVE || application.getState == Application.State.INCOMPLETE
      val inPostProcessing = !(application.getRedoPostProcess() == Application.PostProcessingState.DONE || application.getRedoPostProcess() == null)
      isActiveHakuPeriod && stateUpdateable && !inPostProcessing && userOid == application.getPersonOid
    }

    override def updateHakemus(applicationSystem: ApplicationSystem)(hakemus: HakemusMuutos, userOid: String)(implicit lang: Language.Language): Option[Hakemus] = {
      val applicationQuery: Application = new Application().setOid(hakemus.oid)
      val applicationJavaObject: Option[Application] = timed({
        dao.find(applicationQuery).toList.headOption
      }, 1000, "Application fetch DAO")

      timed({
        applicationJavaObject
          .filter(application => canUpdate(applicationSystem, application, userOid))
          .map { application =>
          val originalAnswers: Hakemus.Answers = application.getAnswers.toMap.mapValues(_.toMap)
          ApplicationUpdater.update(applicationSystem)(application, hakemus)
          timed({
            applicationService.updateAuthorizationMeta(application, false)
          }, 1000, "ApplicationService update authorization Meta")
          timed({
            dao.update(applicationQuery, application)
          }, 1000, "Application update DAO")
          auditLogger.log(UpdateHakemus(userOid, hakemus.oid, originalAnswers, application.getAnswers.toMap.mapValues(_.toMap)))
          hakemusConverter.convertToHakemus(applicationSystem, HakuConverter.convertToHaku(applicationSystem), application)
        }
      }, 1000, "Application update")
    }

    override def findStoredApplicationByOid(oid: String): Application = {
      val applications = timed({
        dao.find(new Application().setOid(oid)).toList
      }, 1000, "Application fetch DAO")
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
      timed({
        val applicationJavaObjects: List[Application] = timed({
          dao.find(query).toList
        }, 1000, "Application fetch DAO")
        applicationJavaObjects.filter{
          application => {
            !application.getState.equals(Application.State.PASSIVE)
          }
        }.map(application => {
          val hakuOption = timed({
            hakuRepository.getHakuByApplication(application)
          }, 1000, "HakuRepository get haku")
          hakuOption.map { case (applicationSystem: ApplicationSystem, haku: Haku) => {
            val hakemus = hakemusConverter.convertToHakemus(applicationSystem, haku, application)
            auditLogger.log(ShowHakemus(application.getPersonOid, hakemus.oid))
            hakemus
          }}
        }).flatten.toList.sortBy[Long](_.received).reverse
      }, 1000, "Application fetch")
    }

    override def exists(personOid: String, hakuOid: String, hakemusOid: String) = {
      dao.find(new Application().setPersonOid(personOid).setOid(hakemusOid).setApplicationSystemId(hakuOid)).size() == 1
    }
  }

}
