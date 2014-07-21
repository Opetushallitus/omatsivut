package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.domain.Hakemus

import scala.collection.JavaConversions._

protected case class ApplicationDaoWrapper(implicit val appConfig: AppConfig) {
  private val dao = appConfig.springContext.applicationDAO

  def findByPersonOid(personOid: String): List[Hakemus] = {
    val applicationJavaObjects: List[Application] = dao.find(new Application().setPersonOid(personOid)).toList
    applicationJavaObjects.map{ application =>
      HakemusConverter.convertToHakemus(HakuRepository().getHakuById(application.getApplicationSystemId))(application)
    }
  }

  def updateApplication(hakemus: Hakemus) = {
    val applicationQuery: Application = new Application().setOid(hakemus.oid)
    val applicationJavaObjects: List[Application] = dao.find(applicationQuery).toList
    applicationJavaObjects.foreach { application =>
      ApplicationUpdater.update(application, hakemus)
      dao.update(applicationQuery, application)
    }
    hakemus
  }
}