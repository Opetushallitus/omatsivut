package fi.vm.sade.omatsivut.hakemus

import java.util.Date

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut._
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.auditlog.{ShowHakemus, UpdateHakemus, AuditLogger}
import fi.vm.sade.omatsivut.domain.Hakemus

case class HakemusRepository(implicit val appConfig: AppConfig) extends Logging {
  import collection.JavaConversions._
  private val dao = appConfig.springContext.applicationDAO

  def updateHakemus(applicationSystem: ApplicationSystem)(hakemus: Hakemus, userOid: String): Hakemus = {
    val updatedHakemus = hakemus.copy(updated = new Date().getTime)
    val applicationQuery: Application = new Application().setOid(updatedHakemus.oid)
    val applicationJavaObjects: List[Application] = dao.find(applicationQuery).toList

    applicationJavaObjects.foreach { application =>
      val originalAnswers: Hakemus.Answers = application.getAnswers().toMap.mapValues(_.toMap)
      ApplicationUpdater.update(applicationSystem)(application, updatedHakemus)
      dao.update(applicationQuery, application)
      AuditLogger.auditLog(UpdateHakemus(userOid, updatedHakemus.oid, originalAnswers, application.getAnswers().toMap.mapValues(_.toMap)))
    }
    updatedHakemus
  }

  def fetchHakemukset(personOid: String): List[Hakemus] = {
    val applicationJavaObjects: List[Application] = dao.find(new Application().setPersonOid(personOid)).toList
    applicationJavaObjects.map{ application => {
      val hakemus = HakemusConverter.convertToHakemus(HakuRepository().getHakuById(application.getApplicationSystemId))(application)
      AuditLogger.auditLog(ShowHakemus(personOid, hakemus.oid))
      hakemus
    }}
  }
}