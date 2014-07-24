package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut._
import fi.vm.sade.omatsivut.domain.Hakemus
import java.util.Date

case class HakemusRepository(implicit val appConfig: AppConfig) extends Logging {
  import collection.JavaConversions._
  private val dao = appConfig.springContext.applicationDAO

  def updateHakemus(applicationSystem: ApplicationSystem)(hakemus: Hakemus): Hakemus = {
    val updatedHakemus = hakemus.copy(updated = new Date().getTime)
    val applicationQuery: Application = new Application().setOid(updatedHakemus.oid)
    val applicationJavaObjects: List[Application] = dao.find(applicationQuery).toList

    applicationJavaObjects.foreach { application =>
      ApplicationUpdater.update(applicationSystem)(application, updatedHakemus)
      dao.update(applicationQuery, application)
    }
    updatedHakemus
  }

  def fetchHakemukset(personOid: String): List[Hakemus] = {
    val applicationJavaObjects: List[Application] = dao.find(new Application().setPersonOid(personOid)).toList
    applicationJavaObjects.map{ application =>
      HakemusConverter.convertToHakemus(HakuRepository().getHakuById(application.getApplicationSystemId))(application)
    }
  }
}