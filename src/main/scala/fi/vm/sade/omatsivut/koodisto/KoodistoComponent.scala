package fi.vm.sade.omatsivut.koodisto

import fi.vm.sade.haku.virkailija.lomakkeenhallinta.koodisto.impl.KoodistoServiceImpl
import fi.vm.sade.koodisto.util.CachingKoodistoClient
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.config.SpringContextComponent
import fi.vm.sade.omatsivut.domain.Language.Language
import scala.collection.JavaConverters._

trait KoodistoComponent {
  this: SpringContextComponent =>

  def koodistoService: KoodistoService

  class RemoteKoodistoService(appConfig: AppConfig) extends KoodistoService {
    lazy val client = new CachingKoodistoClient(appConfig.settings.koodistoUrl)
    lazy val service = new KoodistoServiceImpl(client, springContext.organizationService)

    def postOffices(implicit lang: Language) = {
      service.getPostOffices.asScala.map((office: fi.vm.sade.haku.oppija.lomake.domain.elements.questions.Option) => (office.getValue, office.getI18nText.getTranslations().get(lang.toString))).toMap
    }
  }
}
