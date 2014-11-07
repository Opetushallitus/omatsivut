package fi.vm.sade.omatsivut.koodisto

import fi.vm.sade.haku.virkailija.lomakkeenhallinta.koodisto.impl.{KoodistoServiceMockImpl, KoodistoServiceImpl}
import fi.vm.sade.koodisto.util.CachingKoodistoClient
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.config.SpringContextComponent
import fi.vm.sade.omatsivut.domain.Language.Language
import fi.vm.sade.omatsivut.memoize.{TTLOptionalMemoize, TTLCache}
import scala.collection.JavaConverters._

trait KoodistoService {
  type Translations = Map[String, String]
  private val cacheTimeSec = 60*15

  def postOffice(postalCode: String) = officesMemo().get.get(postalCode)

  private val officesMemo = TTLOptionalMemoize.memoize(getOffices _, "koodisto offices", cacheTimeSec, 64)
  val service: fi.vm.sade.haku.virkailija.lomakkeenhallinta.koodisto.KoodistoService

  private def getOffices() : Option[Map[String, Map[String, String]]] = {
    Some(service.getPostOffices.asScala.map { (office: fi.vm.sade.haku.oppija.lomake.domain.elements.questions.Option) =>
      (office.getValue, office.getI18nText.getTranslations().asScala.toMap)
    }.toMap)
  }
}

trait KoodistoComponent {
  this: SpringContextComponent =>

  def koodistoService: KoodistoService

  class RemoteKoodistoService(appConfig: AppConfig) extends KoodistoService {
    lazy val client = new CachingKoodistoClient(appConfig.settings.koodistoUrl)
    lazy val service = new KoodistoServiceImpl(client, springContext.organizationService)
  }

  class StubbedKoodistoService extends KoodistoService {
    lazy val service = new KoodistoServiceMockImpl
  }
}
