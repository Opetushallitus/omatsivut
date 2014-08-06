package fi.vm.sade.omatsivut.hakemus

import java.util.Date
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut._
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.auditlog.{ShowHakemus, UpdateHakemus, AuditLogger}
import fi.vm.sade.omatsivut.domain.{Hakemus, Language}

case class HakemusRepository(implicit val appConfig: AppConfig) extends Logging {
  import collection.JavaConversions._
  private val dao = appConfig.springContext.applicationDAO

  def canUpdate(applicationSystem: ApplicationSystem)(implicit lang: Language.Language) = {
    val haku = HakuConverter.convertToHaku(applicationSystem)
    haku.applicationPeriods.exists(hakuAika => {
      hakuAika.active
    })
  }

  def updateHakemus(applicationSystem: ApplicationSystem)(hakemus: Hakemus, userOid: String)(implicit lang: Language.Language): Hakemus = {
    val updatedHakemus = hakemus.copy(updated = new Date().getTime)
    val applicationQuery: Application = new Application().setOid(updatedHakemus.oid)
    val applicationJavaObjects: List[Application] = dao.find(applicationQuery).toList

    applicationJavaObjects.foreach { application =>
      val originalAnswers: Hakemus.Answers = application.getAnswers().toMap.mapValues(_.toMap)
      ApplicationUpdater.update(applicationSystem)(application, updatedHakemus)
      dao.update(applicationQuery, application)
      AuditLogger.log(UpdateHakemus(userOid, updatedHakemus.oid, originalAnswers, application.getAnswers().toMap.mapValues(_.toMap)))
    }
    updatedHakemus
  }

  def fetchHakemukset(personOid: String)(implicit lang: Language.Language): List[Hakemus] = {
    val applicationJavaObjects: List[Application] = dao.find(new Application().setPersonOid(personOid)).toList
    applicationJavaObjects.map{ application => {
      val hakemus = HakemusConverter.convertToHakemus(HakuRepository().getHakuById(application.getApplicationSystemId))(application)
      AuditLogger.log(ShowHakemus(personOid, hakemus.oid))
      hakemus
    }}
  }
}