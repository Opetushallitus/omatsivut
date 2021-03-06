package fi.vm.sade.omatsivut.config

import java.time.Duration
import java.util
import java.util.Optional

import fi.vm.sade.haku.oppija.configuration.UrlConfiguration
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.hakemus.it.dao.ApplicationOidDAO
import fi.vm.sade.haku.oppija.hakemus.service.HakuPermissionService
import fi.vm.sade.haku.oppija.hakemus.service.impl.HakuPermissionServiceMockImpl
import fi.vm.sade.haku.oppija.lomake.domain.User
import fi.vm.sade.haku.oppija.lomake.service.Session
import fi.vm.sade.haku.oppija.lomake.service.impl.SystemSession
import fi.vm.sade.haku.virkailija.authentication.AuthenticationService
import fi.vm.sade.haku.virkailija.authentication.impl.AuthenticationServiceMockImpl
import fi.vm.sade.haku.virkailija.valinta.ValintaService
import fi.vm.sade.haku.virkailija.valinta.dto.{HakemusDTO, HakijaDTO}
import fi.vm.sade.koodisto.util.{CachingKoodistoClient, KoodistoClient}
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.mongo.OmatSivutMongoConfiguration
import fi.vm.sade.properties.OphProperties
import fi.vm.sade.utils.slf4j.Logging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation._
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.env.{MapPropertySource, MutablePropertySources}

import scala.collection.JavaConversions._

object OmatSivutSpringContext extends Logging {
  def check {}

  def createApplicationContext(configuration: AppConfig): AnnotationConfigApplicationContext = {
    val appContext: AnnotationConfigApplicationContext = new AnnotationConfigApplicationContext
    logger.info("Using spring configuration " + configuration.springConfiguration)
    appContext.getEnvironment.setActiveProfiles(configuration.springConfiguration.profile)
    customPropertiesHack(appContext, configuration)
    appContext.register(configuration.springConfiguration.getClass)
    val urlConfiguration = new UrlConfiguration()
    OphProperties.merge(urlConfiguration.overrides, configuration.settings.toProperties)
    appContext.getBeanFactory.registerSingleton(classOf[UrlConfiguration].getCanonicalName, urlConfiguration)
    appContext.refresh()
    appContext
  }

  private def customPropertiesHack(appContext: AnnotationConfigApplicationContext, configuration: AppConfig) {
    val configurer: PropertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer()
    val sources: MutablePropertySources = new MutablePropertySources()

    sources.addFirst(new MapPropertySource("omatsivut custom props", mapAsJavaMap(configuration.settings.toProperties)))
    configurer.setPropertySources(sources)
    appContext.addBeanFactoryPostProcessor(configurer)
  }

  @Configuration
  @ComponentScan(basePackages = Array(
    "fi.vm.sade.haku.http",
    "fi.vm.sade.haku.oppija.common",
    "fi.vm.sade.haku.oppija.hakemus",
    "fi.vm.sade.haku.oppija.lomake",
    "fi.vm.sade.haku.oppija.repository",
    "fi.vm.sade.haku.virkailija",
    "fi.vm.sade.haku.oppija.common.koulutusinformaatio.impl"
  ))
  @Import(Array(classOf[OmatSivutMongoConfiguration], classOf[OmatSivutCacheConfiguration]))
  class Dev extends OmatSivutConfiguration {
    val profile = "dev"

    @Bean def applicationOidDAO: ApplicationOidDAO = new ApplicationOidDAO {
      override def generateNewOid() = "1.2.246.562.11.00000441369"
    }

    @Bean def virkailijaAuditLogger: fi.vm.sade.haku.VirkailijaAuditLogger = new fi.vm.sade.haku.VirkailijaAuditLogger

    @Bean def oppijaAuditLogger: fi.vm.sade.haku.OppijaAuditLogger = new fi.vm.sade.haku.OppijaAuditLogger

    @Bean def apiAuditLogger: fi.vm.sade.haku.ApiAuditLogger = new fi.vm.sade.haku.ApiAuditLogger

    @Bean def hakumaksuService = null

    @Bean def sendMailService = null

  }

  @Configuration
  @ComponentScan(basePackages = Array(
    "fi.vm.sade.security",
    "fi.vm.sade.haku.http",
    "fi.vm.sade.haku.oppija.lomake",
    "fi.vm.sade.haku.oppija.repository",
    "fi.vm.sade.haku.oppija.hakemus.it.dao",
    "fi.vm.sade.haku.oppija.hakemus.converter",
    "fi.vm.sade.haku.oppija.hakemus.service",
    "fi.vm.sade.haku.oppija.common.koulutusinformaatio",
    "fi.vm.sade.haku.virkailija.koulutusinformaatio",
    "fi.vm.sade.haku.virkailija.lomakkeenhallinta.i18n",
    "fi.vm.sade.haku.virkailija.viestintapalvelu",
    "fi.vm.sade.haku.oppija.common.organisaatio",
    "fi.vm.sade.haku.virkailija.lomakkeenhallinta.ohjausparametrit",
    "fi.vm.sade.haku.virkailija.lomakkeenhallinta.tarjonta.impl",
    "fi.vm.sade.haku.oppija.common.suoritusrekisteri.impl",
    "fi.vm.sade.haku.virkailija.authentication.impl",
    "fi.vm.sade.haku.virkailija.lomakkeenhallinta.koodisto.impl"

  ),
  excludeFilters = Array(new ComponentScan.Filter(`type` = FilterType.ASSIGNABLE_TYPE, value = Array[Class[_]](classOf[Session])))
  )
  @Import(Array(classOf[OmatSivutMongoConfiguration], classOf[OmatSivutCacheConfiguration]))
  class Default extends OmatSivutConfiguration {
    val profile = "default"

    @Bean def authenticationService: AuthenticationService = new AuthenticationServiceMockImpl

    @Bean def hakuPermissionService: HakuPermissionService = new HakuPermissionServiceMockImpl

    @Bean def userSession: Session = new SystemSession {
      override def getUser(): User = new User("HAKIJA")
    }

    @Bean def virkailijaAuditLogger: fi.vm.sade.haku.VirkailijaAuditLogger = new fi.vm.sade.haku.VirkailijaAuditLogger

    @Bean def oppijaAuditLogger: fi.vm.sade.haku.OppijaAuditLogger = new fi.vm.sade.haku.OppijaAuditLogger

    @Bean def apiAuditLogger: fi.vm.sade.haku.ApiAuditLogger = new fi.vm.sade.haku.ApiAuditLogger

    @Bean def valintaService: ValintaService = new ValintaService {
      override def getHakemus(asOid: String, applicationOid: String): HakemusDTO = unsupportedIntegrationException

      override def getHakija(asOid: String, application: String): HakijaDTO = unsupportedIntegrationException

      override def fetchValintaData(application: Application, valintaTimeout: Optional[Duration]): util.Map[String, String] = unsupportedIntegrationException

      override def getHakijaFromValintarekisteri(asOid: String, application: String): HakijaDTO = unsupportedIntegrationException
    }

    @Bean def koodistoClient(@Value("${host.virkailija}") hostVirkailija: String): KoodistoClient =
      new CachingKoodistoClient(s"https://$hostVirkailija").setCallerId(AppConfig.callerId)

    def unsupportedIntegrationException: Nothing = {
      throw new scala.UnsupportedOperationException("This integration is unsupported and should not be called in omatsivut")
    }
  }

  class Cloud extends Default {
    override val profile = "cloud"
  }

}

trait OmatSivutConfiguration {
  def profile: String // <- should be able to get from annotation
}
