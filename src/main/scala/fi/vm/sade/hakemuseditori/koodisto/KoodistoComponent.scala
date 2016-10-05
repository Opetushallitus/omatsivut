package fi.vm.sade.hakemuseditori.koodisto

import fi.vm.sade.haku.virkailija.lomakkeenhallinta.koodisto.impl.{KoodistoServiceMockImpl, KoodistoServiceImpl}
import fi.vm.sade.koodisto.util.CachingKoodistoClient
import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.hakemus.{HakemusSpringContext, SpringContextComponent}
import fi.vm.sade.hakemuseditori.memoize.{TTLOptionalMemoize, TTLCache}
import scala.collection.JavaConverters._

trait KoodistoService {
  type Translations = Map[String, String]
  private val cacheTimeSec = 60*15

  def postOfficeTranslations(postalCode: String): Option[Map[String, String]] = {
    officesMemo().get.get(postalCode)
  }

  def postOffice(postalCode: String, lang: Language): Option[PostOffice] = {
    val office = postOfficeTranslations(postalCode)
    office.map{(translations: Map[String, String]) =>
      PostOffice(postalCode, translations.getOrElse(lang.toString, ""))
    }
  }

  private val officesMemo = TTLOptionalMemoize.memoize(getOffices _, "koodisto offices", cacheTimeSec, 64)
  val service: fi.vm.sade.haku.virkailija.lomakkeenhallinta.koodisto.KoodistoService

  private def getOffices() : Option[Map[String, Map[String, String]]] = {
    Some(service.getPostOffices.asScala.map { (office: fi.vm.sade.haku.oppija.lomake.domain.elements.questions.Option) =>
      (office.getValue, office.getI18nText.getTranslations().asScala.toMap)
    }.toMap)
  }
}

trait KoodistoComponent {
  def koodistoService: KoodistoService
}

class RemoteKoodistoService(koodistoUrl: String, springContext: HakemusSpringContext, clientSubSystemCode: String) extends KoodistoService {
  lazy val client = new CachingKoodistoClient(koodistoUrl).setClientSubSystemCode(clientSubSystemCode)
  lazy val service = new KoodistoServiceImpl(client, springContext.organizationService)
}

class StubbedKoodistoService extends KoodistoService {
  lazy val service = new KoodistoServiceMockImpl
}

case class PostOffice(postalCode: String, postOffice: String)