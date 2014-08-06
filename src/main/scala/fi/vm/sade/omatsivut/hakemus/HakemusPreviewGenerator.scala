package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.Logging

case class HakemusPreviewGenerator(implicit val appConfig: AppConfig) extends Logging {
  private val dao = appConfig.springContext.applicationDAO

  def generatePreview(personOid: String, applicationOid: String): Option[String] = {
    import collection.JavaConversions._
    val applicationQuery: Application = new Application().setOid(applicationOid).setPersonOid(personOid)
    dao.find(applicationQuery).toList.headOption.map {
      application => "<div>preview</div>"
    }
  }
}
