package fi.vm.sade.omatsivut.hakemus

import java.util.Date
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut._
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.auditlog.{ShowHakemus, UpdateHakemus, AuditLogger}
import fi.vm.sade.omatsivut.domain.{Tulokset, Hakemus, Language}
import fi.vm.sade.omatsivut.ohjausparametrit.OhjausparametritService

case class HakemusRepository(implicit val appConfig: AppConfig) extends Logging {
  import collection.JavaConversions._
  private val dao = appConfig.springContext.applicationDAO

  def canUpdate(applicationSystem: ApplicationSystem, application: Application)(implicit lang: Language.Language) = {
    val haku = HakuConverter.convertToHaku(applicationSystem)
    val isActiveHakuPeriod = haku.applicationPeriods.exists(hakuAika => hakuAika.active)
    val stateUpdateable = application.getState == Application.State.ACTIVE || application.getState == Application.State.INCOMPLETE
    val inPostProcessing = !(application.getRedoPostProcess() == Application.PostProcessingState.DONE || application.getRedoPostProcess() == null)
    isActiveHakuPeriod && stateUpdateable && !inPostProcessing
  }

  def updateHakemus(applicationSystem: ApplicationSystem)(hakemus: Hakemus, userOid: String)(implicit lang: Language.Language): Option[Hakemus] = {
    val updatedHakemus = hakemus.copy(updated = new Date().getTime)
    val applicationQuery: Application = new Application().setOid(updatedHakemus.oid)
    val applicationJavaObject: Option[Application] = dao.find(applicationQuery).toList.headOption

    applicationJavaObject
    .filter(application => canUpdate(applicationSystem, application))
    .map { application =>
      val originalAnswers: Hakemus.Answers = application.getAnswers.toMap.mapValues(_.toMap)
      ApplicationUpdater.update(applicationSystem)(application, updatedHakemus)
      dao.update(applicationQuery, application)
      AuditLogger.log(UpdateHakemus(userOid, updatedHakemus.oid, originalAnswers, application.getAnswers.toMap.mapValues(_.toMap)))
      updatedHakemus
    }
  }

  def fetchHakemukset(personOid: String)(implicit lang: Language.Language): List[Hakemus] = {
    val applicationJavaObjects: List[Application] = dao.find(new Application().setPersonOid(personOid)).toList
    applicationJavaObjects.filter{
        application => {
          !application.getState.equals(Application.State.PASSIVE)
        }
    }.map(application => HakuRepository().getHakuById(application.getApplicationSystemId).map(haku => {
          val hakemus = HakemusConverter.convertToHakemus(haku)(application)
          AuditLogger.log(ShowHakemus(personOid, hakemus.oid))
          hakemus
        }
      )).flatten
  }
}