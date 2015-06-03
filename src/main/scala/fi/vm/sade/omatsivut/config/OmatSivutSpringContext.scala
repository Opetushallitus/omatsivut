package fi.vm.sade.omatsivut.config

import java.util
import java.util.Date

import fi.vm.sade.haku.oppija.common.suoritusrekisteri.{ArvosanaDTO, OpiskelijaDTO, SuoritusDTO, SuoritusrekisteriService}
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.hakemus.it.dao.ApplicationOidDAO
import fi.vm.sade.haku.oppija.hakemus.service.HakuPermissionService
import fi.vm.sade.haku.oppija.hakemus.service.impl.HakuPermissionServiceMockImpl
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.virkailija.authentication.AuthenticationService
import fi.vm.sade.haku.virkailija.authentication.impl.AuthenticationServiceMockImpl
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.tarjonta.HakuService
import fi.vm.sade.haku.virkailija.valinta.ValintaService
import fi.vm.sade.haku.virkailija.valinta.dto.{HakemusDTO, HakijaDTO}
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.mongo.OmatSivutMongoConfiguration
import fi.vm.sade.utils.slf4j.Logging
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
    "fi.vm.sade.haku.oppija.common",
    "fi.vm.sade.haku.oppija.hakemus",
    "fi.vm.sade.haku.oppija.lomake",
    "fi.vm.sade.haku.oppija.repository",
    "fi.vm.sade.haku.virkailija",
    "fi.vm.sade.haku.oppija.common.koulutusinformaatio.impl"
  ))
  @Import(Array(classOf[OmatSivutMongoConfiguration], classOf[OmatSivutCacheConfiguration]))
  @ImportResource(Array("/META-INF/spring/logger-mock-context.xml"))
  class Dev extends OmatSivutConfiguration {
    val profile = "dev"

    @Bean def applicationOidDAO: ApplicationOidDAO = new ApplicationOidDAO {
      override def generateNewOid() = "1.2.246.562.11.00000441369"
    }
  }

  @Configuration
  @ComponentScan(basePackages = Array(
    "fi.vm.sade.security",
    "fi.vm.sade.haku.oppija.lomake",
    "fi.vm.sade.haku.oppija.repository",
    "fi.vm.sade.haku.oppija.hakemus.it.dao",
    "fi.vm.sade.haku.oppija.hakemus.converter",
    "fi.vm.sade.haku.oppija.hakemus.service",
    "fi.vm.sade.haku.oppija.common.koulutusinformaatio",
    "fi.vm.sade.haku.virkailija.koulutusinformaatio",
    "fi.vm.sade.haku.virkailija.lomakkeenhallinta.i18n",
    "fi.vm.sade.haku.oppija.common.organisaatio"
  ))
  @ImportResource(Array("/META-INF/spring/logger-context.xml"))
  @Import(Array(classOf[OmatSivutMongoConfiguration], classOf[OmatSivutCacheConfiguration]))
  class Default extends OmatSivutConfiguration {
    val profile = "default"

    @Bean def authenticationService: AuthenticationService = new AuthenticationServiceMockImpl

    @Bean def hakuPermissionService: HakuPermissionService = new HakuPermissionServiceMockImpl

    @Bean def suoritusRekisteriService: SuoritusrekisteriService = new SuoritusrekisteriService {

      override def getArvosanat(s: String): util.List[ArvosanaDTO] = unsupportedIntegrationException

      override def getOpiskelijatiedot(s: String): util.List[OpiskelijaDTO] = unsupportedIntegrationException

      override def getSuoritukset(s: String): util.Map[String, util.List[SuoritusDTO]] = unsupportedIntegrationException

      override def getSuoritukset(s: String, s1: String): util.Map[String, util.List[SuoritusDTO]] = unsupportedIntegrationException

      override def getSuoritukset(s: String, s1: String, date: Date): util.Map[String, util.List[SuoritusDTO]] = unsupportedIntegrationException

      override def getChanges(s: String, date: Date): util.List[String] = unsupportedIntegrationException
    }

    @Bean def hakuService: HakuService = new HakuService {

      override def getRelatedApplicationOptionIds(s: String): util.List[String] = unsupportedIntegrationException

      override def getApplicationSystems: util.List[ApplicationSystem] = unsupportedIntegrationException

      override def getApplicationSystems(b: Boolean): util.List[ApplicationSystem] = unsupportedIntegrationException

      override def getApplicationSystem(s: String): ApplicationSystem = unsupportedIntegrationException
    }

    @Bean def valintaService: ValintaService = new ValintaService {
      override def getHakemus(asOid: String, applicationOid: String): HakemusDTO = unsupportedIntegrationException

      override def getHakija(asOid: String, application: String): HakijaDTO = unsupportedIntegrationException

      override def fetchValintaData(application: Application): util.Map[String, String] = unsupportedIntegrationException
    }

    def unsupportedIntegrationException: Nothing = {
      throw new scala.UnsupportedOperationException("This integration is supported and should not be called in omatsivut")
    }
  }
}

trait OmatSivutConfiguration {
  def profile: String // <- should be able to get from annotation
}